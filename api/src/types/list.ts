import { User } from "../entities/user";
import { ERROR_MESSAGES } from './errorMessages';

export type RegisterListData = {
    name: string;
    description?: string | null;
    recurring?: boolean;
    sharedUserIds?: number[];
    metadata?: Record<string, any>;
    owner?: User;
};

export interface ListFilterOptions {
    user: User;
    owner?: boolean; 
    search?: string;
    recurring?: boolean;
    page?: number;
    per_page?: number;
    sort_by?: "name" | "created_at" | "updated_at" | "last_purchased_at";
    order?: "ASC" | "DESC";
}

export interface ListUpdateData {
    name?: string;
    description?: string | null;
    recurring?: boolean;
    metadata?: Record<string, any>;
    sharedUserIds?: number[];
}

export function isValidListData(body: any): { isValid: boolean; message?: string } {
    if (!body || typeof body !== "object") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.REQUIRED("body") };
    }
    if (!body.name || typeof body.name !== "string") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("name") };
    }
    if ('description' in body && body.description !== null && typeof body.description !== "string") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("description") };
    }

    if ('is_recurring' in body && typeof body.is_recurring !== "boolean") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("is_recurring") };
    }

    if ('shared_user_ids' in body && !Array.isArray(body.shared_user_ids)) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("shared_user_ids") };
    }

    if (body.metadata && typeof body.metadata !== "object") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("metadata") };
    }
    return { isValid: true };
}

export function isValidListId(params: any): { isValid: boolean; message?: string } {
    if (!params.id || params.id === undefined || params.id === null || params.id === '') {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("ID") };
    }
    
    const id = parseInt(params.id, 10);
    if (isNaN(id) || id < 0) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID };
    }
    
    return { isValid: true };
}
