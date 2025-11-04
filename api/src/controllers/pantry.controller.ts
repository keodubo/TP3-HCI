import { Request, Response } from "express";
import * as PantryService from "../services/pantry.service";
import { replySuccess, replyCreated, replyWithError } from "../http";
import { BadRequestError } from "../types/errors";
import { User } from "../entities/user";
import { ERROR_MESSAGES } from '../types/errorMessages';
import { isValidPantryData, isValidPantryId, isValidUserId, RegisterPantryData } from '../types/pantry';
import { isValidEmail } from '../types/user';
import { Mailer } from '../services/email.service';

export async function createPantry(req: Request, res: Response): Promise<void> {
    try {
        const validationBody = isValidPantryData(req.body);
        if (!validationBody.isValid) {
            throw new BadRequestError(validationBody.message);
        }

        const sharedIds = Array.isArray(req.body.shared_user_ids)
            ? req.body.shared_user_ids.map((id: string | number) => Number(id)).filter(id => !Number.isNaN(id))
            : [];

        const pantryData: RegisterPantryData = {
            name: req.body.name,
            description: req.body.description ?? null,
            metadata: req.body.metadata ?? null,
            sharedUserIds: sharedIds,
            owner: req.user as User,
        };

        const pantry = await PantryService.createPantryService(pantryData, pantryData.owner);
        replyCreated(res, pantry);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function getPantries(req: Request, res: Response): Promise<void> {
    try {
        const user = req.user as User;
        let owner: boolean | undefined = undefined;
        if (typeof req.query.owner === "string") {
            if (req.query.owner === "true") owner = true;
            else if (req.query.owner === "false") owner = false;
        }
        const allowedSortBy = ["created_at", "updated_at", "name"];
        const sortBy = req.query.sort_by ? String(req.query.sort_by) : "created_at";
        const sort_by = allowedSortBy.includes(sortBy) ? sortBy as "created_at" | "updated_at" | "name" : "created_at";
        const order = req.query.order && ["ASC", "DESC"].includes(String(req.query.order).toUpperCase()) ? String(req.query.order).toUpperCase() as "ASC" | "DESC" : "ASC";
        const page = req.query.page ? Math.max(1, Number(req.query.page)) : 1;
        const per_page = req.query.per_page ? Math.max(1, Number(req.query.per_page)) : 10;
        const search = req.query.search ? String(req.query.search) : undefined;
        const result = await PantryService.getPantriesService({
            user,
            owner,
            search,
            sort_by,
            order,
            page,
            per_page,
        });
        replySuccess(res, result);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function getPantryById(req: Request, res: Response): Promise<void> {
    try {
        const user = req.user as User;
        const validation = isValidPantryId(req.params);
        if (!validation.isValid) {
            throw new BadRequestError(validation.message);
        }
        const pantryId = Number(req.params.id);
        const pantry = await PantryService.getPantryByIdService(pantryId, user);
        replySuccess(res, pantry);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function updatePantry(req: Request, res: Response): Promise<void> {
    try {
        const user = req.user as User;
        const validation = isValidPantryId(req.params);
        if (!validation.isValid) {
            throw new BadRequestError(validation.message);
        }
        const pantryId = Number(req.params.id);
        
        const bodyValidation = isValidPantryData(req.body);
        if (!bodyValidation.isValid) {
            throw new BadRequestError(bodyValidation.message);
        }

        const sharedIds = Array.isArray(req.body.shared_user_ids)
            ? req.body.shared_user_ids.map((id: string | number) => Number(id)).filter(id => !Number.isNaN(id))
            : undefined;

        const updateData = {
            name: req.body.name,
            description: req.body.description ?? null,
            metadata: req.body.metadata ?? null,
            sharedUserIds: sharedIds,
        };

        const pantry = await PantryService.updatePantryService(pantryId, updateData, user);
        replySuccess(res, pantry);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function deletePantry(req: Request, res: Response): Promise<void> {
    try {
        const user = req.user as User;
        const validation = isValidPantryId(req.params);
        if (!validation.isValid) {
            throw new BadRequestError(validation.message);
        }
        const pantryId = Number(req.params.id);
        await PantryService.deletePantryService(pantryId, user);
        replySuccess(res, {});
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function sharePantry(req: Request, res: Response): Promise<void> {
    try {
        const user = req.user as User;
        const validation = isValidPantryId(req.params);
        if (!validation.isValid) {
            throw new BadRequestError(validation.message);
        }
        const pantryId = Number(req.params.id);
        
        const recipients: string[] = Array.isArray(req.body.recipients) ? req.body.recipients : [];
        if (recipients.length === 0) {
            throw new BadRequestError(ERROR_MESSAGES.VALIDATION.REQUIRED("recipients"));
        }

        for (const email of recipients) {
            const validation = isValidEmail(String(email));
            if (!validation.isValid) {
                throw new BadRequestError(validation.message);
            }
        }

        const mailer: Mailer = req.app.locals.mailer;
        const sharedPantry = await PantryService.sharePantryService(pantryId, user, recipients, mailer);
        replySuccess(res, sharedPantry);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function sharedUsersPantry(req: Request, res: Response): Promise<void> {
    try {
        const user = req.user as User;
        const validation = isValidPantryId(req.params);
        if (!validation.isValid) {
            throw new BadRequestError(validation.message);
        }
        const pantryId = Number(req.params.id);
        const users = await PantryService.getSharedUsersService(pantryId, user);
        replySuccess(res, users);
    } catch (err) {
        replyWithError(res, err);
    }
}

export async function revokeSharePantry(req: Request, res: Response): Promise<void> {
    try {
        const user = req.user as User;
        const pantryValidation = isValidPantryId(req.params);
        if (!pantryValidation.isValid) {
            throw new BadRequestError(pantryValidation.message);
        }
        const userValidation = isValidUserId(req.params);
        if (!userValidation.isValid) {
            throw new BadRequestError(userValidation.message);
        }
        const pantryId = Number(req.params.id);
        const userId = Number(req.params.user_id);
        await PantryService.revokePantryShareService(pantryId, user, userId);
        replySuccess(res, {});
    } catch (err) {
        replyWithError(res, err);
    }
}
