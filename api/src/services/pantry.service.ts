import AppDataSource from "../db";
import { Pantry } from "../entities/pantry";
import { User } from "../entities/user";
import {NotFoundError, handleCaughtError, BadRequestError, ConflictError, ForbiddenError} from "../types/errors";
import {Product} from "../entities/product";
import {PantryItem} from "../entities/pantryItem";
import { ERROR_MESSAGES } from '../types/errorMessages';
import { In, QueryFailedError } from 'typeorm';
import { Mailer, EmailType } from './email.service';
import { PaginatedResponse, createPaginationResponse } from '../types/pagination';
import { PantryFilterOptions, PantryUpdateData, RegisterPantryData } from '../types/pantry';

/**
 * Creates a new pantry for the given user.
 * Runs inside a transaction to avoid race conditions.
 *
 * @param {{ name: string, metadata?: any }} data - Pantry creation data
 * @param {User} user - Authenticated user
 * @returns {Promise<Pantry>} The created pantry (formatted)
 * @throws {Error} If any error occurs during the process
 */
export async function createPantryService(data: RegisterPantryData, user: User): Promise<any> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const pantry = new Pantry();
        pantry.name = data.name;
        pantry.description = data.description ?? null;
        pantry.metadata = data.metadata ?? null;
        pantry.owner = user;

        if (data.sharedUserIds && data.sharedUserIds.length > 0) {
            const sharedUsers = await queryRunner.manager.find(User, {
                where: data.sharedUserIds.map(id => ({ id })),
            });
            pantry.sharedWith = sharedUsers.filter(shared => shared.id !== user.id);
        }
        await queryRunner.manager.save(pantry);
        await queryRunner.commitTransaction();
        const refreshed = await Pantry.findOne({
            where: { id: pantry.id },
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category"]
        });
        return (refreshed ?? pantry).getFormattedPantry();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        
        if (err instanceof QueryFailedError && err.driverError?.message?.includes('UNIQUE constraint failed: pantry.name, pantry.ownerId')) {
            throw new ConflictError(ERROR_MESSAGES.CONFLICT.PANTRY_EXISTS);
        }
        
        handleCaughtError(err);
    } finally {
        await queryRunner.release();
    }
}

/**
 * Retrieves all pantries owned by the user.
 *
 * @param {User} user - Authenticated user
 * @param {boolean} owner - If true, only return pantries where user is owner; if false, only where sharedWith; if undefined, return all
 * @param {"createdAt" | "updatedAt" | "name"} sort_by - Field to sort by
 * @param {"ASC" | "DESC"} order - Sort order
 * @param {number} page - Page number for pagination
 * @param {number} per_page - Number of items per page for pagination
 * @returns {Promise<Pantry[]>} Array of formatted pantries
 * @throws {Error} If any error occurs during the process
 */
export async function getPantriesService(options: PantryFilterOptions): Promise<PaginatedResponse<any>> {
    const perPage = options.per_page && options.per_page > 0 ? options.per_page : 10;
    const page = options.page && options.page > 0 ? options.page : 1;

    const queryBuilder = Pantry.createQueryBuilder("pantry")
        .leftJoinAndSelect("pantry.owner", "owner")
        .leftJoinAndSelect("pantry.sharedWith", "sharedWith")
        .leftJoinAndSelect("pantry.items", "items")
        .leftJoinAndSelect("items.product", "itemProduct")
        .leftJoinAndSelect("itemProduct.category", "itemCategory")
        .where("pantry.deletedAt IS NULL");

    if (options.owner === true) {
        queryBuilder.andWhere("owner.id = :userId", { userId: options.user.id });
    } else if (options.owner === false) {
        queryBuilder.andWhere("sharedWith.id = :userId", { userId: options.user.id });
    } else {
        queryBuilder.andWhere("owner.id = :userId OR sharedWith.id = :userId", { userId: options.user.id });
    }

    if (options.search) {
        queryBuilder.andWhere("LOWER(pantry.name) LIKE :search", { search: `%${options.search.toLowerCase()}%` });
    }

    const orderDirection = options.order ?? "ASC";
    let orderField: string;
    switch (options.sort_by) {
        case "updated_at":
            orderField = "pantry.updatedAt";
            break;
        case "name":
            orderField = "pantry.name";
            break;
        case "created_at":
        default:
            orderField = "pantry.createdAt";
            break;
    }

    queryBuilder.orderBy(orderField, orderDirection);
    queryBuilder.take(perPage).skip((page - 1) * perPage);

    const [pantries, total] = await queryBuilder.getManyAndCount();
    const formattedPantries = pantries.map(p => p.getFormattedPantry());

    return createPaginationResponse(formattedPantries, total, page, perPage);
}

/**
 * Retrieves a pantry by its ID, only if it belongs to the user.
 *
 * @param {number} pantryId - Pantry ID
 * @param {User} user - Authenticated user
 * @returns {Promise<Pantry>} The formatted pantry
 * @throws {NotFoundError} If the pantry is not found or does not belong to the user
 */
export async function getPantryByIdService(pantryId: number, user: User): Promise<any> {
    const pantry = await Pantry.findOne({
        where: { id: pantryId, deletedAt: null },
        relations: [
            "owner",
            "sharedWith",
            "items",
            "items.product",
            "items.product.category"
        ]
    });
    if (!pantry) {
        throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
    }
    const canAccess =
        pantry.owner.id === user.id ||
        (pantry.sharedWith && pantry.sharedWith.some(u => u.id === user.id));
    if (!canAccess) {
        throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
    }
    return pantry.getFormattedPantry();
}

/**
 * Updates a pantry's name and metadata. Only the owner can update.
 *
 * @param {number} pantryId - Pantry ID
 * @param {string} name - New name
 * @param {any} metadata - New metadata
 * @param {User} user - Authenticated user
 * @returns {Promise<Pantry>} The updated pantry (formatted)
 * @throws {NotFoundError} If the pantry is not found or does not belong to the user
 */
export async function updatePantryService(pantryId: number, data: PantryUpdateData, user?: User): Promise<any> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const pantry = await queryRunner.manager.findOne(Pantry, { where: { id: pantryId, deletedAt: null }, relations: ["owner", "sharedWith"] });
        if (!pantry) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
        }
        
        if (user && pantry.owner.id !== user.id) {
            throw new ForbiddenError(ERROR_MESSAGES.AUTHORIZATION.INSUFFICIENT_PERMISSIONS);
        }
        
        if (data.name !== undefined) pantry.name = data.name;
        if (data.description !== undefined) pantry.description = data.description;
        if (data.metadata !== undefined) pantry.metadata = data.metadata;

        if (data.sharedUserIds !== undefined) {
            const sharedUsers = await queryRunner.manager.find(User, {
                where: (data.sharedUserIds || []).map(id => ({ id })),
            });
            pantry.sharedWith = sharedUsers.filter(shared => shared.id !== pantry.owner.id);
        }

        await queryRunner.manager.save(pantry);
        await queryRunner.commitTransaction();

        const refreshed = await Pantry.findOne({
            where: { id: pantry.id },
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category"]
        });

        return (refreshed ?? pantry).getFormattedPantry();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        
        if (err instanceof QueryFailedError && err.driverError?.message?.includes('UNIQUE constraint failed: pantry.name, pantry.ownerId')) {
            throw new ConflictError(ERROR_MESSAGES.CONFLICT.PANTRY_EXISTS);
        }
        
        handleCaughtError(err);
    } finally {
        await queryRunner.release();
    }
}

/**
 * Deletes a pantry and all its items (soft delete). Only the owner can delete.
 *
 * @param {number} pantryId - Pantry ID
 * @param {User} user - Authenticated user
 * @returns {Promise<void>} Nothing
 * @throws {NotFoundError} If the pantry is not found or does not belong to the user
 */
export async function deletePantryService(pantryId: number, user: User): Promise<void> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const pantry = await queryRunner.manager.findOne(Pantry, { where: { id: pantryId, deletedAt: null }, relations: ["owner"] });
        if (!pantry) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
        }
        
        if (user && pantry.owner.id !== user.id) {
            throw new ForbiddenError(ERROR_MESSAGES.AUTHORIZATION.INSUFFICIENT_PERMISSIONS);
        }

        const pantryItems = await queryRunner.manager.find(PantryItem, { where: { pantry: { id: pantryId }, deletedAt: null } });
        for (const item of pantryItems) {
            await queryRunner.manager.softRemove(PantryItem, item);
        }
        const products = await queryRunner.manager.find(Product, { where: { pantry: { id: pantryId }, deletedAt: null } });
        for (const product of products) {
            product.pantry = null;
            await product.save()
        }
        await queryRunner.manager.softRemove(pantry);
        await queryRunner.commitTransaction();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
    } finally {
        await queryRunner.release();
    }
}

/**
 * Shares a pantry with another user by email. Only the owner can share.
 *
 * @param {number} pantryId - Pantry ID
 * @param {User} user - Authenticated user (owner)
 * @param {string} email - Email of the user to share with
 * @param {Mailer} mailer - Mailer service to send the verification email
 * @returns {Promise<User>} The user the pantry was shared with
 * @throws {NotFoundError} If the pantry or user is not found, or if sharing with the owner
 */
export async function sharePantryService(pantryId: number, user: User, recipients: string[], mailer?: Mailer): Promise<any> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const pantry = await queryRunner.manager.findOne(Pantry, {
            where: { id: pantryId, deletedAt: null },
            relations: ["sharedWith", "owner", "items", "items.product", "items.product.category"]
        });
        if (!pantry || (pantry.owner.id !== user.id && !pantry.sharedWith.some(u => u.id === user.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
        }
        const normalizedEmails = recipients
            .map(email => email?.trim().toLowerCase())
            .filter((email): email is string => !!email);

        if (normalizedEmails.length === 0) {
            throw new BadRequestError(ERROR_MESSAGES.VALIDATION.REQUIRED("recipients"));
        }

        const existingShared = new Map(pantry.sharedWith?.map(u => [u.id, u]) || []);
        const newUsers: User[] = [];

        for (const email of normalizedEmails) {
            const toUser = await queryRunner.manager.findOne(User, { where: { email, deletedAt: null } });
            if (!toUser) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
            if (toUser.id === user.id) throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.CANNOT_SHARE_WITH_YOURSELF);
            if (existingShared.has(toUser.id)) continue;
            existingShared.set(toUser.id, toUser);
            newUsers.push(toUser);
        }

        if (newUsers.length === 0) {
            await queryRunner.commitTransaction();
            return pantry.getFormattedPantry();
        }

        pantry.sharedWith = Array.from(existingShared.values());
        await queryRunner.manager.save(pantry);
        await queryRunner.commitTransaction();

        if (mailer) {
            for (const toUser of newUsers) {
                try {
                    await mailer.sendEmail(
                        EmailType.PANTRY_SHARED,
                        toUser.displayName,
                        pantry.name,
                        user.displayName
                    );
                } catch (emailError) {
                    console.error('Failed to send pantry shared notification email:', emailError);
                }
            }
        }

        const refreshed = await Pantry.findOne({
            where: { id: pantry.id },
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category"]
        });

        return (refreshed ?? pantry).getFormattedPantry();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Retrieves all users the pantry is shared with. Only the owner can view.
 *
 * @param {number} pantryId - Pantry ID
 * @param {User} user - Authenticated user (owner)
 * @returns {Promise<User[]>} Array of shared users
 * @throws {NotFoundError} If the pantry is not found or does not belong to the user
 */
export async function getSharedUsersService(pantryId: number, user: User): Promise<PaginatedResponse<any>> {
    const pantry = await Pantry.findOne({
        where: { id: pantryId, deletedAt: null },
        relations: ["owner", "sharedWith"]
    });

    if (!pantry || pantry.owner.id !== user.id) {
        throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
    }

    const summaries = (pantry.sharedWith || []).map(sharedUser => sharedUser.getSummary());
    return createPaginationResponse(summaries, summaries.length, 1, summaries.length || 1);
}

/**
 * Revokes a user's access to a shared pantry. Only the owner can revoke.
 *
 * @param {number} pantryId - Pantry ID
 * @param {User} user - Authenticated user (owner)
 * @param {number} toUserId - ID of the user to revoke
 * @returns {Promise<void>} Nothing
 * @throws {NotFoundError} If the pantry or user is not found, or if the user is not shared
 */
export async function revokePantryShareService(pantryId: number, user: User, toUserId: number): Promise<void> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const pantry = await queryRunner.manager.findOne(Pantry, { where: { id: pantryId, owner: { id: user.id }, deletedAt: null }, relations: ["sharedWith", "owner"] });
        if (!pantry) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
        const toUser = await queryRunner.manager.findOne(User, { where: { id: toUserId, deletedAt: null } });
        if (!toUser) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
        const wasShared = pantry.sharedWith.some(u => u.id === toUserId);
        if (!wasShared) throw new BadRequestError("Pantry not shared with user");
        pantry.sharedWith = pantry.sharedWith.filter(u => u.id !== toUserId);
        await queryRunner.manager.save(pantry);
        await queryRunner.commitTransaction();
        return;
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
        throw err;
    } finally {
        await queryRunner.release();
    }
}
