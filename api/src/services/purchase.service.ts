import AppDataSource from "../db";
import { Purchase } from "../entities/purchase";
import { List } from "../entities/list";
import { User } from "../entities/user";
import { NotFoundError, BadRequestError, handleCaughtError } from "../types/errors";
import { GetPurchasesData } from "../types/purchase";
import {ListItem} from "../entities/listItem";
import { ERROR_MESSAGES } from '../types/errorMessages';
import { PaginatedResponse, createPaginationResponse } from '../types/pagination';
import { generateUniqueListName } from '../utils/listNameUtils';

/**
 * Retrieves purchases with optional filters and pagination.
 *
 * @param {GetPurchasesData} filter - Purchase data for filtering and pagination
 * @returns {Promise<Category>} Purchase information
 * @throws {NotFoundError} If purchase is not found
 */
export async function getPurchasesService(filter: GetPurchasesData): Promise<PaginatedResponse<any>> {
  try {
    const perPage = filter.per_page && filter.per_page > 0 ? filter.per_page : 10;
    const page = filter.page && filter.page > 0 ? filter.page : 1;

    const queryBuilder = Purchase.createQueryBuilder("purchase")
      .leftJoinAndSelect("purchase.list", "list")
      .leftJoinAndSelect("list.owner", "listOwner")
      .leftJoinAndSelect("list.sharedWith", "listShared")
      .leftJoinAndSelect("purchase.owner", "owner")
      .leftJoinAndSelect("purchase.items", "items")
      .leftJoinAndSelect("items.product", "product")
      .leftJoinAndSelect("product.category", "category")
      .where("purchase.ownerId = :ownerId", { ownerId: filter.user.id })
      .withDeleted();

    if (filter.list_id) {
      queryBuilder.andWhere("purchase.listId = :listId", { listId: filter.list_id });
    }

    const orderDirection = filter.order ?? "DESC";
    let orderField: string;
    switch (filter.sort_by) {
      case "restored_at":
        orderField = "purchase.restoredAt";
        break;
      case "list_name":
        orderField = "list.name";
        break;
      case "id":
        orderField = "purchase.id";
        break;
      case "created_at":
      default:
        orderField = "purchase.createdAt";
        break;
    }

    queryBuilder.orderBy(orderField, orderDirection);
    queryBuilder.skip((page - 1) * perPage).take(perPage);

    const [purchases, total] = await queryBuilder.getManyAndCount();

    for (const purchase of purchases) {
      if (!purchase.list && purchase.listId) {
        purchase.list = await AppDataSource.getRepository(List).findOne({
          where: { id: purchase.listId },
          relations: ["owner", "sharedWith"],
          withDeleted: true
        });
      }
    }

    const formattedPurchases = purchases.map(p => p.getFormattedPurchase());

    return createPaginationResponse(formattedPurchases, total, page, perPage);
  } catch (err) {
    handleCaughtError(err);
    throw err;
  }
}

/**
 * Retrieves a purchase by its ID for a specific user.
 *
 * @param {number} id - Purchase ID
 * @param {User} user - Authenticated user
 * @returns {Promise<Purchase>} Purchase information
 * @throws {NotFoundError} If purchase is not found
 */
export async function getPurchaseByIdService(id: number, user: User): Promise<any> {
  try {
    const purchase = await AppDataSource.getRepository(Purchase)
        .createQueryBuilder("purchase")
        .leftJoinAndSelect("purchase.owner", "owner")
        .leftJoinAndSelect("purchase.items", "items")
        .leftJoinAndSelect("items.product", "product")
        .leftJoinAndSelect("product.category", "productCategory")
        .leftJoinAndSelect("product.pantry", "productPantry")
        .leftJoinAndSelect("productPantry.owner", "productPantryOwner")
        .where("purchase.id = :id", { id })
        .andWhere("purchase.ownerId = :ownerId", { ownerId: user.id })
        .withDeleted()
        .getOne();

    if (!purchase) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PURCHASE);

    if (purchase.listId) {
      purchase.list = await AppDataSource.getRepository(List).findOne({
        where: { id: purchase.listId },
        relations: ["owner", "sharedWith"],
        withDeleted: true
      });
    }

    return purchase.getFormattedPurchase();
  } catch (err) {
    handleCaughtError(err);
    throw err;
  }
}

/**
 * Restores a purchase by creating a new list from a previous purchase (non-recurring only).
 *
 * @param {number} id - Purchase ID
 * @param {User} user - Authenticated user
 * @returns {Promise<List>} Restored purchased list information
 * @throws {NotFoundError} If purchase is not found
 */
export async function restorePurchaseService(id: number, user: User): Promise<any> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();

  try {
    let purchase = await queryRunner.manager
        .getRepository(Purchase)
        .createQueryBuilder("purchase")
        .leftJoinAndSelect("purchase.list", "list")
        .leftJoinAndSelect("purchase.items", "items")
        .leftJoinAndSelect("items.product", "product")
        .leftJoinAndSelect("product.category", "productCategory")
        .leftJoinAndSelect("product.pantry", "productPantry")
        .leftJoinAndSelect("productPantry.owner", "productPantryOwner")
        .leftJoinAndSelect("purchase.owner", "owner")
        .addSelect("purchase.listId")
        .where("purchase.id = :id", { id })
        .andWhere("purchase.ownerId = :ownerId", { ownerId: user.id })
        .withDeleted()
        .getOne();

    if (!purchase) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PURCHASE);

    if (!purchase.list && purchase.listId) {
      purchase.list = await queryRunner.manager.findOne(List, {
        where: { id: purchase.listId },
        relations: ["owner", "sharedWith", "items"],
        withDeleted: true,
      });
    }

    if (!purchase.list) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.LIST);
    if (purchase.list.recurring) throw new BadRequestError(ERROR_MESSAGES.BUSINESS_RULE.CANNOT_RESTORE_RECURRING_LIST);

    const newList = new List();
    newList.name = await generateUniqueListName(purchase.list.name, user, queryRunner);
    newList.description = purchase.list.description;
    newList.recurring = false;
    newList.metadata = purchase.list.metadata;
    newList.owner = user;

    await queryRunner.manager.save(newList);

    if (purchase.items && purchase.items.length > 0) {
      for (const item of purchase.items) {
        const newItem = new ListItem();
        Object.assign(newItem, item);
        newItem.id = undefined;
        newItem.quantity = item.quantity;
        newItem.unit = item.unit;
        newItem.metadata = item.metadata;
        newItem.purchased = false;
        newItem.lastPurchasedAt = null;
        newItem.product = item.product;
        newItem.owner = user;
        newItem.list = newList;
        newItem.purchase = null;

        await queryRunner.manager.save(newItem);
      }
    }

    purchase.list = newList;
    purchase.listId = newList.id;
    purchase.restoredAt = new Date();
    await queryRunner.manager.save(purchase);

    await queryRunner.commitTransaction();
    const refreshed = await Purchase.findOne({
      where: { id: purchase.id },
      relations: ["list", "list.owner", "list.sharedWith", "items", "items.product", "items.product.category"],
      withDeleted: true,
    });

    return (refreshed ?? purchase).getFormattedPurchase();
  } catch (err) {
    if (queryRunner.isTransactionActive) await queryRunner.rollbackTransaction();
    handleCaughtError(err);
    throw err;
  } finally {
    await queryRunner.release();
  }
}
