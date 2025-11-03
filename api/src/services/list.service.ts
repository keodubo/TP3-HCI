import AppDataSource from "../db";
import {BadRequestError, handleCaughtError, NotFoundError, ConflictError} from "../types/errors";
import {User} from "../entities/user";
import {List} from "../entities/list";
import {ListFilterOptions, ListUpdateData, RegisterListData} from "../types/list";
import {ListItem} from "../entities/listItem";
import {removeUserForListShared, removeUserPrivateValues} from "../utils/users";
import {PantryItem} from "../entities/pantryItem";
import {Pantry} from "../entities/pantry";
import {In} from "typeorm";
import {Purchase} from "../entities/purchase";
import { ERROR_MESSAGES } from '../types/errorMessages';
import { Mailer, EmailType } from './email.service';
import { PaginatedResponse, createPaginationResponse } from '../types/pagination';

/**
 * Creates a new list.
 * Runs inside a transaction to avoid race conditions.
 *
 * @param {RegisterListData} listData - Creation data
 * @returns {Promise<{ list: List }>} Created list
 * @throws {Error} If any error occurs during the process
 */
export async function createListService(listData: RegisterListData): Promise<List> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        const list = new List();
        list.name = listData.name;
        list.description = listData.description ?? null;
        list.recurring = listData.recurring ?? false;
        list.metadata = listData.metadata ?? null;
        list.owner = listData.owner!;

        if (listData.sharedUserIds && listData.sharedUserIds.length > 0) {
            const sharedUsers = await queryRunner.manager.find(User, {
                where: listData.sharedUserIds.map(id => ({ id })),
            });
            list.sharedWith = sharedUsers.filter(user => user.id !== list.owner.id);
        }

        await queryRunner.manager.save(list);
        await queryRunner.commitTransaction();

        const saved = await List.findOne({ where: { id: list.id }, relations: ["owner", "sharedWith", "items"] });
        return (saved ?? list).getFormattedList();
    } catch (err: any) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        
        if (err.code === 'SQLITE_CONSTRAINT' && err.message.includes('unique_list_name_per_owner')) {
            throw new ConflictError(ERROR_MESSAGES.CONFLICT.LIST_NAME_EXISTS);
        }
        
        handleCaughtError(err);
    } finally {
        await queryRunner.release();
    }
}

/**
 * Retrieves shopping lists based on filter options.
 *
 * @param {ListFilterOptions} listData - Authenticated user object
 * @returns {Promise<List[]>} List information
 * @throws {NotFoundError} If the list is not found
 */
export async function getListsService(listData: ListFilterOptions): Promise<PaginatedResponse<any>> {
    try {
        const perPage = listData.per_page && listData.per_page > 0 ? listData.per_page : 10;
        const page = listData.page && listData.page > 0 ? listData.page : 1;
        const queryBuilder = List.createQueryBuilder("list")
            .leftJoinAndSelect("list.owner", "owner")
            .leftJoinAndSelect("list.sharedWith", "sharedWith")
            .leftJoinAndSelect("list.items", "items")
            .leftJoinAndSelect("items.product", "itemProduct")
            .leftJoinAndSelect("itemProduct.category", "itemCategory")
            .leftJoinAndSelect("itemProduct.pantry", "itemPantry")
            .andWhere("list.deletedAt IS NULL");

        if (listData.owner === true) {
            queryBuilder.andWhere("list.owner.id = :userId", { userId: listData.user.id });
        } else if (listData.owner === false) {
            queryBuilder.andWhere("sharedWith.id = :userId", { userId: listData.user.id });
        } else {
            queryBuilder.andWhere(
                "(list.owner.id = :userId OR sharedWith.id = :userId)",
                { userId: listData.user.id }
            );
        }

        if (listData.search) {
            queryBuilder.andWhere("list.name LIKE :search", { search: `%${listData.search}%` });
        }
        if (listData.recurring !== undefined) {
            queryBuilder.andWhere("list.recurring = :recurring", { recurring: listData.recurring });
        }

        let orderField: string;
        switch (listData.sort_by) {
            case "created_at":
                orderField = "list.createdAt";
                break;
            case "updated_at":
                orderField = "list.updatedAt";
                break;
            case "last_purchased_at":
                orderField = "list.lastPurchasedAt";
                break;
            case "name":
            default:
                orderField = "list.name";
        }
        const orderDirection = listData.order ?? "ASC";

        queryBuilder
            .orderBy(orderField, orderDirection)
            .take(perPage)
            .skip((page - 1) * perPage);

        const [lists, total] = await queryBuilder.getManyAndCount();

        const formattedLists = lists.map(list => list.getFormattedList());

        return createPaginationResponse(
            formattedLists,
            total,
            page,
            perPage
        );
    } catch (err) {
        handleCaughtError(err);
    }
}

/**
 * Retrieves a shopping list by ID.
 * @param {number} listId - List ID
 * @param {User} user - Authenticated user
 * @returns {Promise<List>} List information
 * @throws {NotFoundError} If the list is not found or not accessible
 */
export async function getListByIdService(listId: number, user: User): Promise<List> {
    try {
        const list = await List.findOne({
            where: { id: listId },
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category", "items.product.pantry"]
        });
        if (!list || (list.owner.id !== user.id && !list.sharedWith.some(u => u.id === user.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }
        return list.getFormattedList();
    } catch (err) {
        handleCaughtError(err);
    }
}

/**
 * Updates the list. Runs inside a tx to avoid race condition.
 *
 * @param {number} listId - List ID
 * @param {ListUpdateData} data - New list data
 * @param {User} user - Authenticated user
 * @returns {Promise<List>} Updated list object
 * @throws {NotFoundError} If the list is not found or not accessible
 */
export async function updateListService(listId: number, data: ListUpdateData, user: User): Promise<List> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const list = await queryRunner.manager.findOne(List, {
            where: { id: listId } as any,
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category", "items.product.pantry"],
        });
        if (!list || (list.owner.id !== user.id && !list.sharedWith.some(u => u.id === user.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }
        if (data.name !== undefined) list.name = data.name;
        if (data.description !== undefined) list.description = data.description;
        if (data.recurring !== undefined) list.recurring = data.recurring;
        if (data.metadata !== undefined) list.metadata = data.metadata;

        if (data.sharedUserIds !== undefined) {
            if (data.sharedUserIds === null) {
                list.sharedWith = [];
            } else {
                const sharedUsers = await queryRunner.manager.find(User, {
                    where: data.sharedUserIds.map(id => ({ id })),
                });
                list.sharedWith = sharedUsers.filter(u => u.id !== list.owner.id);
            }
        }
        await queryRunner.manager.save(list);
        await queryRunner.commitTransaction();
        const refreshed = await queryRunner.manager.findOne(List, {
            where: { id: list.id },
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category", "items.product.pantry"],
        });
        return (refreshed ?? list).getFormattedList();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        throw err;
    } finally {
        await queryRunner.release();
    }
}


/**
 * Deletes a shopping list by ID.
 *
 * @param {User} user - Authenticated user
 * @param {number} listId - List ID
 * @returns {Promise<boolean>} True if deletion was successful
 * @throws {NotFoundError} If the list is not found
 */
export async function deleteListService(listId: number, user: User): Promise<boolean> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    try {
        const list = await queryRunner.manager.findOne(List, { where:
                { id: listId, owner:
                        { id: user.id },
                        deletedAt: null} });
        if (!list) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);

        await queryRunner.manager.softRemove(list);
        await queryRunner.commitTransaction();
        return true;
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Marks a shopping list as purchased and, if not recurring, moves it to purchase history.
 *
 * @param {number} listId - Shopping list ID
 * @param {User} user - Authenticated user
 * @param {Record<string, any>} metadata - Metadata for the purchase
 * @returns {Promise<List>} The updated or moved list
 * @throws {NotFoundError} If the list is not found or not accessible
 */
export async function purchaseListService(listId: number, user: User, metadata: Record<string, any> | undefined): Promise<List> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const list = await queryRunner.manager.findOne(List, {
            where: { id: listId },
            relations: ["items", "owner", "sharedWith"]
        });
        if (!list || (list.owner.id !== user.id && !list.sharedWith.some(u => u.id === user.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }

        if(list.items.length <= 0) {
            throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.NO_ITEMS_IN_SHOPPING_LIST);
        }

        const listItems: ListItem[] = [];
        for (const item of list.items) {
            const listItem = await queryRunner.manager.findOne(ListItem, { where: { id: item.id } });
            if (listItem) {
                if(listItem.purchased) {
                    listItem.lastPurchasedAt = new Date();
                    listItems.push(listItem);
                    await queryRunner.manager.save(listItem);
                }
            } else {
                throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.ITEM);
            }
        }

        if(listItems.length <= 0) {
            throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.NO_ITEMS_PURCHASED_IN_SHOPPING_LIST);
        }

        const purchase = new Purchase();
        list.lastPurchasedAt = new Date();
        await queryRunner.manager.save(list);
        purchase.owner = user;
        purchase.list = list;
        purchase.items = listItems;
        purchase.metadata = metadata ?? {};
        await queryRunner.manager.save(purchase);

        if (!list.recurring) {
            await queryRunner.manager.softRemove(list);
        }

        await queryRunner.commitTransaction();
        const refreshed = await List.findOne({
            where: { id: list.id },
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category", "items.product.pantry"],
        });
        return (refreshed ?? list).getFormattedList();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Resets the items of a shopping list, marking them as not purchased.
 *
 * @param {number} listId - Shopping list ID
 * @param {User} user - Authenticated user
 * @returns {Promise<List>} The updated list with reset items
 * @throws {NotFoundError} If the list is not found or not accessible
 */
export async function resetListItemsService(listId: number, user: User): Promise<List> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const list = await queryRunner.manager.findOne(List, {
            where: { id: listId },
            relations: ["items", "owner", "sharedWith"]
        });
        if (!list || (list.owner.id !== user.id && !list.sharedWith.some(u => u.id === user.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }

        for (const item of list.items) {
            item.purchased = false;
            await queryRunner.manager.save(item);
        }

        await queryRunner.commitTransaction();
        const refreshed = await List.findOne({
            where: { id: list.id },
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category", "items.product.pantry"],
        });
        return (refreshed ?? list).getFormattedList();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Moves purchased items of a shopping list to the pantry.
 *
 * @param {number} listId - Shopping list ID
 * @param {User} user - Authenticated user
 * @param {number} listId - Shopping list ID
 * @param {User} user - Authenticated user
 * @param {number} pantryId - Destination pantry ID
 * @param {string | null | undefined} notes - Optional notes metadata
 * @returns {Promise<{ pantry_id: string; items: any[] }>} Transfer summary
 * @throws {NotFoundError} If the list or pantry is not accessible
 */
export async function moveToPantryService(
    listId: number,
    user: User,
    pantryId: number,
    notes?: string | null
): Promise<{ pantry_id: string; items: any[] }> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const list = await queryRunner.manager.findOne(List, {
            where: { id: listId, owner: { id: user.id }, deletedAt: null },
            relations: ["items", "items.product", "items.product.category", "owner"]
        });
        if (!list) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);

        const pantry = await queryRunner.manager.findOne(Pantry, {
            where: { id: pantryId, deletedAt: null },
            relations: ["owner", "sharedWith"]
        });
        if (!pantry) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);

        const canAccessPantry =
            pantry.owner.id === user.id ||
            (pantry.sharedWith && pantry.sharedWith.some(u => u.id === user.id));
        if (!canAccessPantry) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PANTRY);
        }

        const movedItems: PantryItem[] = [];
        for (const item of list.items) {
            if (!item.purchased) continue;

            let pantryItem = await queryRunner.manager.findOne(PantryItem, {
                where: {
                    product: { id: item.product.id },
                    owner: { id: user.id },
                    pantry: { id: pantry.id },
                    deletedAt: null
                }
            });

            if (!pantryItem) {
                pantryItem = new PantryItem();
                pantryItem.product = item.product;
                pantryItem.quantity = item.quantity;
                pantryItem.unit = item.unit ?? item.product.unit ?? null;
                pantryItem.metadata = item.metadata ? { ...item.metadata } : {};
                pantryItem.owner = user;
                pantryItem.pantry = pantry;
            } else {
                pantryItem.quantity += item.quantity;
                if (item.unit && item.unit !== pantryItem.unit) {
                    pantryItem.unit = item.unit;
                }
                if (item.metadata) {
                    pantryItem.metadata = { ...(pantryItem.metadata ?? {}), ...item.metadata };
                }
            }

            if (notes) {
                pantryItem.metadata = { ...(pantryItem.metadata ?? {}), transfer_notes: notes };
            }

            pantryItem.addedAt = new Date();
            await queryRunner.manager.save(pantryItem);
            movedItems.push(pantryItem);
        }

        await queryRunner.commitTransaction();

        if (movedItems.length === 0) {
            return {
                pantry_id: String(pantry.id),
                items: [],
            };
        }

        const refreshedItems = await PantryItem.find({
            where: {
                pantry: { id: pantry.id },
                owner: { id: user.id },
                id: In(movedItems.map(item => item.id)),
            },
            relations: ["product", "product.category", "pantry"]
        });

        const formattedItems = refreshedItems.map(item => item.getFormattedListItem());
        return {
            pantry_id: String(pantry.id),
            items: formattedItems,
        };
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Shares a shopping list with another user.
 *
 * @param {number} listId - Shopping list ID
 * @param {User} fromUser - Authenticated user sharing the list
 * @param {string} toUserEmail - Email of the user to share the list with
 * @param {Mailer} mailer - Mailer service to send the verification email
 * @returns {Promise<List>} The shared list
 * @throws {NotFoundError} If the list or user is not found or not accessible
 */
export async function shareListService(listId: number, fromUser: User, recipientEmails: string[], mailer?: Mailer): Promise<List> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const list = await queryRunner.manager.findOne(List, {
            where: { id: listId },
            relations: ["sharedWith", "owner", "items", "items.product", "items.product.category", "items.product.pantry"]
        });
        if (!list || (list.owner.id !== fromUser.id && !list.sharedWith.some(u => u.id === fromUser.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }

        const normalizedEmails = (recipientEmails || [])
            .map(email => email?.trim().toLowerCase())
            .filter((email): email is string => !!email);

        if (normalizedEmails.length === 0) {
            throw new BadRequestError(ERROR_MESSAGES.VALIDATION.REQUIRED("recipients"));
        }

        const currentSharedMap = new Map(list.sharedWith?.map(u => [u.id, u]) || []);
        const newlyAddedUsers: User[] = [];

        for (const email of normalizedEmails) {
            const toUser = await queryRunner.manager.findOne(User, { where: { email, deletedAt: null } });
            if (!toUser) {
                throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
            }

            if (toUser.id === fromUser.id) {
                throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.CANNOT_SHARE_WITH_YOURSELF);
            }

            if (currentSharedMap.has(toUser.id)) {
                continue;
            }

            currentSharedMap.set(toUser.id, toUser);
            newlyAddedUsers.push(toUser);
        }

        if (newlyAddedUsers.length === 0) {
            await queryRunner.commitTransaction();
            const refreshed = await List.findOne({
                where: { id: list.id },
                relations: ["owner", "sharedWith", "items", "items.product", "items.product.category", "items.product.pantry"],
            });
            return (refreshed ?? list).getFormattedList();
        }

        list.sharedWith = Array.from(currentSharedMap.values());
        await queryRunner.manager.save(list);

        await queryRunner.commitTransaction();

        const refreshed = await List.findOne({
            where: { id: list.id },
            relations: ["owner", "sharedWith", "items", "items.product", "items.product.category", "items.product.pantry"],
        });

        if (mailer) {
            try {
                for (const toUser of newlyAddedUsers) {
                    await mailer.sendEmail(
                        EmailType.LIST_SHARED,
                        toUser.displayName,
                        list.name,
                        fromUser.displayName
                    );
                }
            } catch (emailError) {
                console.error('Failed to send list shared notification email:', emailError);
            }
        }

        return (refreshed ?? list).getFormattedList();
    } catch (err) {
        if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
        handleCaughtError(err);
        throw err;
    } finally {
        await queryRunner.release();
    }
}

/**
 * Retrieves the users with whom a shopping list is shared.
 *
 * @param {number} listId - Shopping list ID
 * @param {User} user - Authenticated user requesting the info
 * @returns {Promise<User[]>} List of users with whom the list is shared
 * @throws {NotFoundError} If the list is not found or not accessible
 */
export async function getSharedUsersService(listId: number, user: User): Promise<PaginatedResponse<any>> {
    try {
        const list = await List.findOne({
            where: { id: listId },
            relations: ["sharedWith", "owner"]
        });

        if (!list) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);

        if (list.owner.id !== user.id && !(list.sharedWith && list.sharedWith.some(u => u.id === user.id))) {
            throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        }

        const summaries = (list.sharedWith || []).map(sharedUser => sharedUser.getSummary());
        return createPaginationResponse(summaries, summaries.length, 1, summaries.length || 1);

    } catch (err) {
        handleCaughtError(err);
        throw err;
    }
}

/**
 * Revokes the access of a user to a shared shopping list.
 *
 * @param {number} listId - Shopping list ID
 * @param {User} fromUser - Authenticated user revoking access
 * @param {number} toUserId - ID of the user whose access is to be revoked
 * @returns {Promise<void>} The updated list
 * @throws {NotFoundError} If the list or user is not found
 */
export async function revokeListShareService(listId: number, fromUser: User, toUserId: number): Promise<void> {
    const queryRunner = AppDataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();
    try {
        const list = await queryRunner.manager.findOne(List, {
            where: { id: listId, owner: { id: fromUser.id }, deletedAt: null },
            relations: ["items", "owner", "sharedWith"]
        });
        if (!list) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
        const wasShared = list.sharedWith.some(u => u.id === toUserId);
        if (!wasShared) throw new BadRequestError("Shopping list not shared with user");

        list.sharedWith = list.sharedWith.filter(user => user.id !== toUserId);
        await queryRunner.manager.save(list);

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
