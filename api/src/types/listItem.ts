import { User } from "../entities/user";
import { ERROR_MESSAGES } from './errorMessages';

export interface RegisterListItemData {
    listId: number;
    owner: User;
    productId: number;
    quantity: number;
    unit?: string | null;
    metadata?: Record<string, any> | null;
}

export interface ListItemUpdateData {
    productId?: number;
    quantity?: number;
    unit?: string | null;
    metadata?: Record<string, any> | null;
}

export interface ListItemPatchData {
    quantity?: number;
    unit?: string | null;
    purchased?: boolean;
}

export interface ListItemFilterOptions {
    listId: number;
    user: User;
    purchased?: boolean;
    page?: number;
    per_page?: number;
    sort_by?: "updated_at" | "created_at" | "last_purchased_at" | "product_name";
    order?: "ASC" | "DESC";
    pantry_id?: number;
    category_id?: number;
    search?: string;
}

export function isValidListItemData(body: any): { isValid: boolean; message?: string } {
    if (!body || typeof body !== "object") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.REQUIRED("body") };
    }

    if (!body.product_id) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("product_id") };
    }
    const productId = Number(body.product_id);
    if (Number.isNaN(productId) || productId <= 0) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID_WITH_TYPE("Product") };
    }

    if (body.quantity === undefined) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("quantity") };
    }
    if (typeof body.quantity !== "number" || Number.isNaN(body.quantity) || body.quantity <= 0) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("quantity") };
    }

    if (body.unit !== undefined && body.unit !== null && (typeof body.unit !== "string" || body.unit.trim() === "")) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("unit") };
    }

    if (body.metadata !== undefined && body.metadata !== null && typeof body.metadata !== "object") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("metadata") };
    }

    return { isValid: true };
}

export function isValidListItemUpdateData(body: any): { isValid: boolean; message?: string } {
    if (!body || typeof body !== "object") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.REQUIRED("body") };
    }

    if (body.product_id !== undefined) {
        const productId = Number(body.product_id);
        if (Number.isNaN(productId) || productId <= 0) {
            return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID_WITH_TYPE("Product") };
        }
    }

    if (body.quantity !== undefined) {
        if (typeof body.quantity !== "number" || Number.isNaN(body.quantity) || body.quantity <= 0) {
            return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("quantity") };
        }
    }

    if (body.unit !== undefined && body.unit !== null && (typeof body.unit !== "string" || body.unit.trim() === "")) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("unit") };
    }

    if (body.metadata !== undefined && body.metadata !== null && typeof body.metadata !== "object") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("metadata") };
    }

    return { isValid: true };
}

export function isValidListItemPatchData(body: any): { isValid: boolean; message?: string } {
    if (!body || typeof body !== "object") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.REQUIRED("body") };
    }

    if (body.quantity !== undefined) {
        if (typeof body.quantity !== "number" || Number.isNaN(body.quantity) || body.quantity <= 0) {
            return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("quantity") };
        }
    }

    if (body.unit !== undefined && body.unit !== null && (typeof body.unit !== "string" || body.unit.trim() === "")) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("unit") };
    }

    if (body.purchased !== undefined && typeof body.purchased !== "boolean") {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("purchased") };
    }

    return { isValid: true };
}

export function isValidListItemId(params: any): { isValid: boolean; message?: string } {
    if (!params.id || params.id === undefined || params.id === null || params.id === '') {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("ID") };
    }
    
    const id = parseInt(params.id, 10);
    if (isNaN(id) || id < 0) {
        return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID };
    }
    return { isValid: true };
}
