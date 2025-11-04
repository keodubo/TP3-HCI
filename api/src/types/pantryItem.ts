import { User } from "../entities/user";
import { ERROR_MESSAGES } from './errorMessages';

export interface RegisterPantryItemData {
  productId: number;
  quantity: number;
  unit?: string | null;
  expirationDate?: Date | null;
  metadata?: Record<string, any> | null;
}

export interface PantryItemUpdateData {
  productId?: number;
  quantity?: number;
  unit?: string | null;
  expirationDate?: Date | null;
  metadata?: Record<string, any> | null;
}

export interface PantryItemFilterOptions {
  pantryId: number;
  user: User;
  page?: number;
  per_page?: number;
  sort_by?: "created_at" | "updated_at" | "product_name" | "quantity";
  order?: "ASC" | "DESC";
  search?: string;
  category_id?: number;
}

export function isValidPantryItemData(body: any): { isValid: boolean; message?: string } {
  if (!body || typeof body !== "object") {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.REQUIRED("body") };
  }
  if (body.product_id === undefined) {
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
  if (body.expiration_date !== undefined && body.expiration_date !== null && isNaN(Date.parse(body.expiration_date))) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("expiration_date") };
  }
  if (body.metadata !== undefined && body.metadata !== null && typeof body.metadata !== "object") {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("metadata") };
  }
  return { isValid: true };
}

export function isValidPantryItemUpdateData(body: any): { isValid: boolean; message?: string } {
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

  if (body.expiration_date !== undefined && body.expiration_date !== null && isNaN(Date.parse(body.expiration_date))) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("expiration_date") };
  }
  
  if (body.metadata !== undefined && body.metadata !== null && typeof body.metadata !== "object") {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("metadata") };
  }
  
  return { isValid: true };
}

export function isValidPantryItemId(params: any): { isValid: boolean; message?: string } {
  if (!params.item_id || params.item_id === undefined || params.item_id === null || params.item_id === '') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("ID") };
  }
  
  const id = parseInt(params.item_id, 10);
  if (isNaN(id) || id < 0) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID_WITH_TYPE("Item") };
  }
  return { isValid: true };
}
