import { Request, Response } from "express";
import { replyCreated, replySuccess, replyWithError } from '../http';
import * as ListService from '../services/list.service';
import { BadRequestError, ServerError } from '../types/errors';
import {
    isValidListData,
    isValidListId,
    RegisterListData,
   ListFilterOptions,
    ListUpdateData
} from "../types/list";
import { User } from "../entities/user";
import { createInvalidIdMessage } from '../types/errorMessages';
import { ERROR_MESSAGES } from '../types/errorMessages';
import { isValidEmail } from '../types/user';
import { Mailer } from '../services/email.service';

export async function registerList(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListData(req.body);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const sharedIds = Array.isArray(req.body.shared_user_ids)
            ? req.body.shared_user_ids.map((id: string | number) => Number(id)).filter((id: number) => !isNaN(id))
            : [];

        const listData: RegisterListData = {
            name: req.body.name,
            description: req.body.description ?? null,
            recurring: req.body.is_recurring ?? false,
            metadata: req.body.metadata,
            sharedUserIds: sharedIds,
            owner: req.user as User,
        };

        const newList = await ListService.createListService(listData);
        replyCreated(res, newList);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function getLists(req: Request, res: Response): Promise<void> {
    try {
        const filterOptions: ListFilterOptions = {
            user: req.user as User,
            owner: req.query.owner !== undefined ? req.query.owner === "true" : undefined,
            search: req.query.search ? String(req.query.search) : undefined,
            recurring: req.query.recurring !== undefined ? req.query.recurring === "true" : undefined,
            page: req.query.page ? Number(req.query.page) : 1,
            per_page: req.query.per_page ? Number(req.query.per_page) : 10,
            sort_by: req.query.sort_by ? String(req.query.sort_by) as "name" | "created_at" | "updated_at" | "last_purchased_at" : "name",
            order: req.query.order ? String(req.query.order).toUpperCase() as "ASC" | "DESC" : "ASC"
        };

        const result = await ListService.getListsService(filterOptions);
        replySuccess(res, result);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function getListById(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const list = await ListService.getListByIdService(Number(req.params.id), req.user as User);
        replySuccess(res, list);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function updateList(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const listData: ListUpdateData = {
            name: req.body.name,
            description: req.body.description ?? null,
            recurring: req.body.is_recurring,
            metadata: req.body.metadata,
            sharedUserIds: Array.isArray(req.body.shared_user_ids)
                ? req.body.shared_user_ids.map((id: string | number) => Number(id)).filter((id: number) => !isNaN(id))
                : undefined,
        };
        const updatedList = await ListService.updateListService(parseInt(req.params.id), listData, req.user as User);
        replySuccess(res, updatedList);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function deleteList(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const deleted = await ListService.deleteListService(Number(req.params.id), req.user as User);
        if (!deleted) throw new ServerError(ERROR_MESSAGES.SERVER.OPERATION_FAILED);

        replySuccess(res, {});
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function purchaseShoppingList(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);
        const user = req.user as User;
        const result = await ListService.purchaseListService(Number(req.params.id), user, req.body.metadata ?? undefined);
        replyCreated(res, result);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function resetShoppingList(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);
        const user = req.user as User;
        const result = await ListService.resetListItemsService(Number(req.params.id), user);
        replySuccess(res, result);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function moveToPantry(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);
        const user = req.user as User;
        const pantryId = Number(req.body?.pantry_id);
        if (!pantryId || Number.isNaN(pantryId)) {
            throw new BadRequestError(ERROR_MESSAGES.VALIDATION.MISSING_FIELD("pantry_id"));
        }
        const result = await ListService.moveToPantryService(Number(req.params.id), user, pantryId, req.body?.notes);
        replySuccess(res, result);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function shareShoppingList(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);

        const recipients: string[] = Array.isArray(req.body.recipients) ? req.body.recipients : [];
        if (recipients.length === 0) {
            throw new BadRequestError(ERROR_MESSAGES.VALIDATION.REQUIRED("recipients"));
        }

        for (const email of recipients) {
            const emailValidation = isValidEmail(String(email));
            if (!emailValidation.isValid) {
                throw new BadRequestError(emailValidation.message);
            }
        }

        const fromUser = req.user as User;
        const mailer: Mailer = req.app.locals.mailer;
        const result = await ListService.shareListService(Number(req.params.id), fromUser, recipients, mailer);
        replySuccess(res, result);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function sharedUsersShoppingList(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);
        const user = req.user as User;
        const users = await ListService.getSharedUsersService(Number(req.params.id), user);
        replySuccess(res, users);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function revokeShareShoppingList(req: Request, res: Response): Promise<void> {
    try {
        const validation = isValidListId(req.params);
        if (!validation.isValid) throw new BadRequestError(validation.message);
        const userId = Number(req.params.user_id);
        if (!userId) throw new BadRequestError(createInvalidIdMessage("User"));
        const fromUser = req.user as User;
        await ListService.revokeListShareService(Number(req.params.id), fromUser, userId);
        replySuccess(res, {});
    } catch (err) {
        replyWithError(res, err);
    }
}
