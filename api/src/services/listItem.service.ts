import AppDataSource from "../db";
import { BadRequestError, NotFoundError, handleCaughtError, ConflictError } from "../types/errors";
import { ListItem } from "../entities/listItem";
import { List } from "../entities/list";
import { Product } from "../entities/product";
import { User } from "../entities/user";
import { ERROR_MESSAGES } from '../types/errorMessages';
import {
    RegisterListItemData,
    ListItemUpdateData,
    ListItemFilterOptions,
    ListItemPatchData,
} from "../types/listItem";
import { PaginatedResponse, createPaginationResponse } from '../types/pagination';

/**
 * Creates a new list item inside a shopping list.
 * Runs inside a transaction to avoid race conditions.
 *
 * @param {RegisterListItemData} itemData - Item data (name, quantity, metadata, listId)
 * @returns {Promise<{ item: ListItem }>} Created item
 * @throws {NotFoundError} If the parent list does not exist
 */
export async function addListItemService(
    itemData: RegisterListItemData
): Promise<any> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        const list = await queryRunner.manager.findOne(List, {
            where: { id: itemData.listId, deletedAt: null },
            relations: ["owner", "sharedWith", "items", "items.product"],
        });
        if (!list) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);

        const canAccess = list.owner.id === itemData.owner.id
            || (list.sharedWith && list.sharedWith.some(u => u.id === itemData.owner.id));
        if (!canAccess) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }

        const product = await queryRunner.manager.findOne(Product, {
            where: { id: itemData.productId, deletedAt: null },
            relations: ["pantry", "pantry.owner"]
        });
        if (!product) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PRODUCT);

        const itemsArray = Array.isArray(list.items) ? list.items : [];
        for (const auxItem of itemsArray) {
            if (auxItem.product.id === product.id) {
                throw new ConflictError(ERROR_MESSAGES.CONFLICT.ITEM_EXISTS);
            }
        }
        const item = new ListItem();
        item.product = product;
        item.quantity = itemData.quantity ?? product.defaultQuantity ?? 1;
        item.unit = itemData.unit ?? product.unit ?? null;
        item.metadata = itemData.metadata ?? null;
        item.purchased = false;
        item.list = list;
        item.owner = itemData.owner;

        await queryRunner.manager.save(item);
        await queryRunner.commitTransaction();

        const saved = await ListItem.findOne({
            where: { id: item.id },
            relations: ["list", "list.owner", "product", "product.pantry", "product.pantry.owner", "product.category"],
        });
        const formatted = saved ? saved.getFormattedListItem() : item.getFormattedListItem();
        return formatted;
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Retrieves items from a list with optional filters (purchased, pagination).
 *
 * @param {ListItemFilterOptions} filterOptions - Filter and pagination options
 * @returns {Promise<ListItem[]>} List of items
 * @throws {NotFoundError} If no items are found
 */
export async function getListItemsService(filterOptions: ListItemFilterOptions): Promise<PaginatedResponse<any>> {
    try {
        const list = await List.findOne({
            where: { id: filterOptions.listId, deletedAt: null },
            relations: ["owner", "sharedWith"],
        });
        if (!list) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);

        const canAccess = list.owner.id === filterOptions.user.id
            || (list.sharedWith && list.sharedWith.some(u => u.id === filterOptions.user.id));
        if (!canAccess) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }

        const perPage = filterOptions.per_page && filterOptions.per_page > 0 ? filterOptions.per_page : 10;
        const page = filterOptions.page && filterOptions.page > 0 ? filterOptions.page : 1;

        const queryBuilder = ListItem.createQueryBuilder("item")
            .leftJoinAndSelect("item.product", "product")
            .leftJoinAndSelect("product.category", "category")
            .leftJoinAndSelect("product.pantry", "pantry")
            .leftJoinAndSelect("item.list", "list")
            .leftJoinAndSelect("list.owner", "owner")
            .where("item.listId = :listId", { listId: filterOptions.listId })
            .andWhere("item.deletedAt IS NULL");

        if (filterOptions.purchased !== undefined) {
            queryBuilder.andWhere("item.purchased = :purchased", { purchased: filterOptions.purchased });
        }

        if (filterOptions.pantry_id !== undefined) {
            queryBuilder.andWhere("product.pantryId = :pantryId", { pantryId: filterOptions.pantry_id });
        }

        if (filterOptions.category_id !== undefined) {
            queryBuilder.andWhere("product.categoryId = :categoryId", { categoryId: filterOptions.category_id });
        }

        if (filterOptions.search) {
            queryBuilder.andWhere("LOWER(product.name) LIKE :search", { search: `%${filterOptions.search.toLowerCase()}%` });
        }

        const orderDirection = filterOptions.order ?? "DESC";
        let orderField: string;
        switch (filterOptions.sort_by) {
            case "updated_at":
                orderField = "item.updatedAt";
                break;
            case "last_purchased_at":
                orderField = "item.lastPurchasedAt";
                break;
            case "product_name":
                orderField = "product.name";
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
    } catch (err) {
        handleCaughtError(err);
    }
}

/**
 * Updates a list item (name, quantity, metadata).
 * Runs inside a transaction to avoid race condition.
 *
 * @param {number} listId - Parent list ID
 * @param {number} itemId - Item ID
 * @param {ListItemUpdateData} updateData - Data to update
 * @returns {Promise<ListItem>} Updated item
 * @throws {NotFoundError} If item is not found
 */
export async function updateListItemService(
    listId: number,
    itemId: number,
    user: User,
    updateData: ListItemUpdateData
): Promise<any> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        const list = await queryRunner.manager
            .createQueryBuilder(List, "list")
            .leftJoinAndSelect("list.owner", "owner")
            .leftJoinAndSelect("list.sharedWith", "sharedWith")
            .where("list.id = :listId", { listId })
            .andWhere("list.deletedAt IS NULL")
            .getOne();
        if (!list) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }

        const canAccess = list.owner.id === user.id
            || (list.sharedWith && list.sharedWith.some(u => u.id === user.id));
        if (!canAccess) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }
        
        const item = await queryRunner.manager.findOne(ListItem, {
            where: { id: itemId, list: { id: listId }, deletedAt: null },
            relations: ["list", "list.owner", "product", "product.pantry", "product.pantry.owner", "product.category"],
        });

        if (!item) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.ITEM);

        if (updateData.productId !== undefined) {
            const newProduct = await queryRunner.manager.findOne(Product, {
                where: { id: updateData.productId, deletedAt: null },
                relations: ["pantry", "pantry.owner", "category"],
            });
            if (!newProduct) {
                throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PRODUCT);
            }
            item.product = newProduct;
        }

        if (updateData.quantity !== undefined) {
            if (typeof updateData.quantity !== "number" || updateData.quantity <= 0) {
                throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.QUANTITY_POSITIVE);
            }
            item.quantity = updateData.quantity;
        }

        if (updateData.unit !== undefined) {
            if (updateData.unit !== null && (typeof updateData.unit !== "string" || updateData.unit.trim() === "")) {
                throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.UNIT_NON_EMPTY);
            }
            item.unit = updateData.unit ? updateData.unit.trim() : null;
        }

        if (updateData.metadata !== undefined) {
            if (typeof updateData.metadata !== "object" && updateData.metadata !== null) {
                throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.METADATA_OBJECT_OR_NULL);
            }
            item.metadata = updateData.metadata;
        }

        await queryRunner.manager.save(item);
        await queryRunner.commitTransaction();

        const refreshed = await queryRunner.manager.findOne(ListItem, {
            where: { id: item.id },
            relations: ["list", "list.owner", "product", "product.pantry", "product.category"],
        });

        return (refreshed ?? item).getFormattedListItem();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Toggles the purchased status of a list item.
 *
 * @param {number} listId - Parent list ID
 * @param {number} itemId - Item ID
 * @param {boolean} purchased - New purchased status
 * @returns {Promise<ListItem>} Updated item
 * @throws {NotFoundError} If item is not found
 */
export async function patchListItemService(
    listId: number,
    itemId: number,
    user: User,
    patchData: ListItemPatchData
): Promise<any> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        const item = await queryRunner.manager.findOne(ListItem, {
            where: { id: itemId, list: { id: listId }, deletedAt: null },
            relations: ["list", "list.owner", "list.sharedWith", "product", "product.pantry", "product.pantry.owner", "product.category"],
        });

        if (!item) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.ITEM);

        const canAccess = item.list.owner.id === user.id
            || (item.list.sharedWith && item.list.sharedWith.some(u => u.id === user.id));
        if (!canAccess) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.ITEM);
        }

        if (patchData.quantity !== undefined) {
            if (typeof patchData.quantity !== "number" || patchData.quantity <= 0) {
                throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.QUANTITY_POSITIVE);
            }
            item.quantity = patchData.quantity;
        }

        if (patchData.unit !== undefined) {
            if (patchData.unit !== null && (typeof patchData.unit !== "string" || patchData.unit.trim() === "")) {
                throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.UNIT_NON_EMPTY);
            }
            item.unit = patchData.unit ? patchData.unit.trim() : null;
        }

        if (patchData.purchased !== undefined) {
            item.purchased = patchData.purchased;
            if (patchData.purchased) {
                item.lastPurchasedAt = new Date();
            } else {
                item.lastPurchasedAt = null;
            }
        }

        await queryRunner.manager.save(item);

        await queryRunner.commitTransaction();
        const refreshed = await queryRunner.manager.findOne(ListItem, {
            where: { id: item.id },
            relations: ["list", "list.owner", "product", "product.pantry", "product.category"],
        });
        return (refreshed ?? item).getFormattedListItem();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Deletes a list item from a shopping list.
 * Runs inside a tx to avoid race condition.
 *
 * @param {number} listId - Parent list ID
 * @param {number} itemId - Item ID
 * @returns {Promise<boolean>} True if deletion was successful
 * @throws {NotFoundError} If item is not found
 */
export async function deleteListItemService(listId: number, itemId: number, user: User): Promise<boolean> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        const item = await queryRunner.manager.findOne(ListItem, {
            where: { id: itemId, list: { id: listId }, deletedAt: null },
            relations: ["list", "list.owner", "list.sharedWith"],
        });

        if (!item) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.ITEM);

        const canAccess = item.list.owner.id === user.id
            || (item.list.sharedWith && item.list.sharedWith.some(u => u.id === user.id));
        if (!canAccess) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.ITEM);
        }

        await queryRunner.manager.softRemove(item);

        await queryRunner.commitTransaction();
        return true;
    } catch (err) {
        await queryRunner.rollbackTransaction();
        handleCaughtError(err);
        throw err;
    } finally {
        await queryRunner.release();
    }
}
