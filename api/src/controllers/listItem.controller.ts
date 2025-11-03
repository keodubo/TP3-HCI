import { Request, Response } from "express";
import { replyCreated, replySuccess, replyWithError } from "../http";
import * as ListItemService from "../services/listItem.service";
import { BadRequestError, ServerError } from "../types/errors";
import {
    isValidListItemData,
    isValidListItemId,
    isValidListItemUpdateData,
    isValidListItemPatchData,
    RegisterListItemData,
    ListItemUpdateData,
    ListItemFilterOptions,
    ListItemPatchData,
} from "../types/listItem";
import { isValidListId } from "../types/list";
import { User } from "../entities/user";
import { ERROR_MESSAGES } from '../types/errorMessages';

/**
 * Get all items from a shopping list.
 */
export async function getListItems(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const allowedSortBy = ["updated_at", "created_at", "last_purchased_at", "product_name"];
        const sortBy = req.query.sort_by ? String(req.query.sort_by) : "created_at";
        const sort_by = allowedSortBy.includes(sortBy)
            ? (sortBy as "updated_at" | "created_at" | "last_purchased_at" | "product_name")
            : "created_at";
        const order = req.query.order && ["ASC", "DESC"].includes(String(req.query.order).toUpperCase()) ? String(req.query.order).toUpperCase() as "ASC" | "DESC" : "DESC";
        const pantry_id = req.query.pantry_id ? Number(req.query.pantry_id) : undefined;
        const category_id = req.query.category_id ? Number(req.query.category_id) : undefined;
        const search = req.query.search ? String(req.query.search) : undefined;

        const filterOptions: ListItemFilterOptions = {
            listId: Number(req.params.id),
            user: req.user as User,
            purchased:
                req.query.purchased !== undefined
                    ? req.query.purchased === "true"
                    : undefined,
            page: req.query.page ? Number(req.query.page) : 1,
            per_page: req.query.per_page ? Number(req.query.per_page) : 10,
            sort_by,
            order,
            pantry_id,
            category_id,
            search,
        };

        const result = await ListItemService.getListItemsService(filterOptions);
        replySuccess(res, result);
    } catch (err) {
        replyWithError(res, err);
    }
}

/**
 * Add a new item to a shopping list.
 */
export async function addListItem(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const bodyValidation = isValidListItemData(req.body);
        if (!bodyValidation.isValid) throw new BadRequestError(bodyValidation.message);

        const itemData: RegisterListItemData = {
            listId: Number(req.params.id),
            owner: req.user as User,
            productId: Number(req.body.product_id),
            quantity: Number(req.body.quantity),
        };
        if (req.body.unit !== undefined) {
            itemData.unit = req.body.unit ?? null;
        }
        if (req.body.metadata !== undefined) {
            itemData.metadata = req.body.metadata ?? null;
        }

        const newItem = await ListItemService.addListItemService(itemData);
        replyCreated(res, newItem);
    } catch (err) {
        replyWithError(res, err);
    }
}

/**
 * Update a shopping list item.
 */
export async function updateListItem(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListItemId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const updateValidation = isValidListItemUpdateData(req.body);
        if (!updateValidation.isValid) throw new BadRequestError(updateValidation.message);

        const listId = Number(req.params.id);
        const itemId = Number(req.params.item_id);
        const updatePayload: ListItemUpdateData = {};
        if (req.body.product_id !== undefined) {
            updatePayload.productId = Number(req.body.product_id);
        }
        if (req.body.quantity !== undefined) {
            updatePayload.quantity = Number(req.body.quantity);
        }
        if (req.body.unit !== undefined) {
            updatePayload.unit = req.body.unit ?? null;
        }
        if (req.body.metadata !== undefined) {
            updatePayload.metadata = req.body.metadata ?? null;
        }

        const updatedItem = await ListItemService.updateListItemService(listId, itemId, req.user as User, updatePayload);
        replySuccess(res, updatedItem);
    } catch (err) {
        replyWithError(res, err);
    }
}

/**
 * Toggle purchased status of a shopping list item.
 */
export async function patchListItem(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListItemId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const patchValidation = isValidListItemPatchData(req.body);
        if (!patchValidation.isValid) throw new BadRequestError(patchValidation.message);

        const listId = Number(req.params.id);
        const itemId = Number(req.params.item_id);

        const patchData: ListItemPatchData = {};
        if (req.body.quantity !== undefined) {
            patchData.quantity = Number(req.body.quantity);
        }
        if (req.body.unit !== undefined) {
            patchData.unit = req.body.unit ?? null;
        }
        if (req.body.purchased !== undefined) {
            patchData.purchased = req.body.purchased;
        }

        const patchedItem = await ListItemService.patchListItemService(listId, itemId, req.user as User, patchData);
        replySuccess(res, patchedItem);
    } catch (err) {
        replyWithError(res, err);
    }
}

/**
 * Delete a shopping list item.
 */
export async function deleteListItem(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListItemId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const listId = Number(req.params.id);
        const itemId = Number(req.params.item_id);

        const deleted = await ListItemService.deleteListItemService(listId, itemId, req.user as User);
        if (!deleted) throw new ServerError(ERROR_MESSAGES.SERVER.OPERATION_FAILED);

        replySuccess(res, {});
    } catch (err) {
        replyWithError(res, err);
    }
}
