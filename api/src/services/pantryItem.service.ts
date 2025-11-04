import AppDataSource from "../db";
import { Pantry } from "../entities/pantry";
import { PantryItem } from "../entities/pantryItem";
import { Product } from "../entities/product";
import { User } from "../entities/user";
import {NotFoundError, handleCaughtError, ConflictError, BadRequestError} from "../types/errors";
import { ERROR_MESSAGES } from '../types/errorMessages';
import { PaginatedResponse, createPaginationResponse } from '../types/pagination';
import { PantryItemFilterOptions, PantryItemUpdateData, RegisterPantryItemData } from "../types/pantryItem";

/**
 * Retrieves pantry items for a given pantry, with support for pagination, sorting, and search.
 *
 * @param {number} pantryId - Pantry ID
 * @param {User} user - Authenticated user
 * @param {number} page - Page number for pagination
 * @param {number} per_page - Results per page
 * @param {'ASC' | 'DESC'} order - Sort order (ASC or DESC)
 * @param {string} [sort_by] - Field to sort by (name, quantity, unit)
 * @param {string} [search] - Search by product name (case-insensitive, partial match)
 * @param {number} [category_id] - Filter by category ID
 * @returns {Promise<{PantryItem[]}>} Paginated pantry items
 * @throws {NotFoundError} If the pantry is not found or not accessible by the user
 */
export async function getPantryItemsService(options: PantryItemFilterOptions): Promise<PaginatedResponse<any>> {
    const pantry = await Pantry.createQueryBuilder("pantry")
        .leftJoinAndSelect("pantry.sharedWith", "sharedWith")
        .leftJoinAndSelect("pantry.owner", "owner")
        .where("pantry.id = :pantryId", { pantryId: options.pantryId })
        .andWhere("pantry.deletedAt IS NULL")
        .getOne();

    if (!pantry) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);

    const canAccess = pantry.owner.id === options.user.id
        || (pantry.sharedWith && pantry.sharedWith.some(u => u.id === options.user.id));
    if (!canAccess) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);

    const perPage = options.per_page && options.per_page > 0 ? options.per_page : 10;
    const page = options.page && options.page > 0 ? options.page : 1;

    const queryBuilder = PantryItem.createQueryBuilder("item")
        .leftJoinAndSelect("item.product", "product")
        .leftJoinAndSelect("product.category", "category")
        .where("item.pantryId = :pantryId", { pantryId: pantry.id })
        .andWhere("item.deletedAt IS NULL");

    if (options.search) {
        queryBuilder.andWhere("LOWER(product.name) LIKE :search", { search: `%${options.search.toLowerCase()}%` });
    }

    if (options.category_id) {
        queryBuilder.andWhere("category.id = :categoryId", { categoryId: options.category_id });
    }

    const orderDirection = options.order ?? "DESC";
    let orderField: string;
    switch (options.sort_by) {
        case "updated_at":
            orderField = "item.updatedAt";
            break;
        case "product_name":
            orderField = "product.name";
            break;
        case "quantity":
            orderField = "item.quantity";
            break;
        case "created_at":
        default:
            orderField = "item.createdAt";
            break;
    }

    queryBuilder.orderBy(orderField, orderDirection);
    queryBuilder.take(perPage).skip((page - 1) * perPage);

    const [items, total] = await queryBuilder.getManyAndCount();
    const formattedItems = items.map(i => i.getFormattedListItem());

    return createPaginationResponse(formattedItems, total, page, perPage);
}

/**
 * Adds a new item to the pantry, or updates the existing item if the product already exists in the pantry.
 * Runs inside a transaction to avoid race conditions.
 *
 * @param {number} pantryId - Pantry ID
 * @param {User} user - Authenticated user
 * @param {{ product_id: number, quantity: number, unit?: string, metadata?: any }} data - Item data
 * @returns {Promise<object>} The created or updated pantry item (id, product_id, quantity, unit, metadata, added_at, pantry_id)
 * @throws {NotFoundError} If the pantry or product is not found or not accessible by the user
 * @throws {BadRequestError} If the item already exists in the pantry
 */
export async function addPantryItemService(pantryId: number, user: User, data: RegisterPantryItemData): Promise<any> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const pantry = await queryRunner.manager
            .createQueryBuilder(Pantry, "pantry")
            .leftJoin("pantry.sharedWith", "sharedWith")
            .leftJoin("pantry.owner", "owner")
            .where("pantry.id = :pantryId", { pantryId })
            .andWhere("pantry.deletedAt IS NULL")
            .andWhere("owner.id = :userId OR sharedWith.id = :userId", { userId: user.id })
            .getOne();
        if (!pantry) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
        const product = await queryRunner.manager.findOne(Product, { where: { id: data.productId, deletedAt: null }, relations: ["category", "pantry", "pantry.owner"]});
        if (!product) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PRODUCT);

        let item = await queryRunner.manager.findOne(PantryItem, {
            where: {
                pantry: { id: pantry.id },
                product: { id: product.id },
                deletedAt: null
            },
            relations: ["pantry", "product"]
        });
        if (!item) {
            item = new PantryItem();
            item.pantry = pantry;
            item.product = product;
            item.quantity = data.quantity;
            const unitSource = data.unit !== undefined ? data.unit : product.unit ?? null;
            item.unit = unitSource ? String(unitSource).trim() : null;
            item.metadata = data.metadata ?? null;
            item.owner = user;
            item.expirationDate = data.expirationDate ?? null;
            item.addedAt = new Date();
            await queryRunner.manager.save(item);
        } else {
            throw new ConflictError(ERROR_MESSAGES.CONFLICT.ITEM_EXISTS);
        }
        await queryRunner.commitTransaction();
        return item.getFormattedListItem();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
    } finally {
        await queryRunner.release();
    }
}

/**
 * Updates an existing pantry item. Only the pantry owner or shared users can update.
 *
 * @param {number} pantryId - Pantry ID
 * @param {number} itemId - Pantry item ID
 * @param {User} user - Authenticated user
 * @param {{ quantity?: number, unit?: string, metadata?: any }} data - Fields to update
 * @returns {Promise<object>} The updated pantry item
 * @throws {NotFoundError} If the item or pantry is not found or not accessible by the user
 */
export async function updatePantryItemService(pantryId: number, itemId: number, user: User, data: PantryItemUpdateData): Promise<any> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const pantry = await queryRunner.manager
            .createQueryBuilder(Pantry, "pantry")
            .leftJoinAndSelect("pantry.sharedWith", "sharedWith")
            .leftJoinAndSelect("pantry.owner", "owner")
            .where("pantry.id = :pantryId", { pantryId })
            .andWhere("pantry.deletedAt IS NULL")
            .getOne();
        if (!pantry || (pantry.owner?.id !== user.id && !pantry.sharedWith?.some(u => u.id === user.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
        }
        
        const item = await queryRunner.manager.findOne(PantryItem, { where: { id: itemId, pantry: { id: pantryId }, deletedAt: null }, relations: ["pantry", "product", "product.category", "product.pantry", "product.pantry.owner"] });
        if (!item) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.ITEM);
        
        if (data.productId !== undefined) {
            const newProduct = await queryRunner.manager.findOne(Product, { where: { id: data.productId, deletedAt: null }, relations: ["category", "pantry"] });
            if (!newProduct) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PRODUCT);
            item.product = newProduct;
        }

        if (data.quantity !== undefined) {
            item.quantity = data.quantity;
        }
        if (data.unit !== undefined) {
            if (data.unit !== null && String(data.unit).trim() === "") {
                throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.UNIT_NON_EMPTY);
            }
            item.unit = data.unit !== null ? String(data.unit).trim() : null;
        }
        if (data.expirationDate !== undefined) {
            item.expirationDate = data.expirationDate ?? null;
        }
        if (data.metadata !== undefined) item.metadata = data.metadata;
        await queryRunner.manager.save(item);
        await queryRunner.commitTransaction();
        const refreshed = await queryRunner.manager.findOne(PantryItem, { where: { id: item.id }, relations: ["product", "product.category", "pantry"] });
        return (refreshed ?? item).getFormattedListItem();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
    } finally {
        await queryRunner.release();
    }
}

/**
 * Deletes a pantry item (soft delete). Only the pantry owner or shared users can delete.
 *
 * @param {number} pantryId - Pantry ID
 * @param {number} itemId - Pantry item ID
 * @param {User} user - Authenticated user
 * @returns {Promise<void>} Nothing
 * @throws {NotFoundError} If the item or pantry is not found or not accessible by the user
 */
export async function deletePantryItemService(pantryId: number, itemId: number, user: User): Promise<void> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const pantry = await queryRunner.manager
            .createQueryBuilder(Pantry, "pantry")
            .leftJoinAndSelect("pantry.sharedWith", "sharedWith")
            .leftJoinAndSelect("pantry.owner", "owner")
            .where("pantry.id = :pantryId", { pantryId })
            .andWhere("pantry.deletedAt IS NULL")
            .getOne();
        
        if (!pantry || (pantry.owner?.id !== user.id && !pantry.sharedWith?.some(u => u.id === user.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
        }
        
        const item = await queryRunner.manager.findOne(PantryItem, { 
            where: { id: itemId, deletedAt: null }, 
            relations: ["pantry"] 
        });
        if (!item || item.pantry.id !== pantryId) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.ITEM);
        
        await queryRunner.manager.softRemove(item);
        await queryRunner.commitTransaction();
        return;
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
    } finally {
        await queryRunner.release();
    }
}
