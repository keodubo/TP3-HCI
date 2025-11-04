import { User } from "../entities/user";
import { ERROR_MESSAGES } from './errorMessages';

export type RegisterPantryData = {
  name: string;
  description?: string | null;
  sharedUserIds?: number[];
  metadata?: Record<string, any>;
  owner: User;
}

export interface PantryFilterOptions {
  user: User;
  owner?: boolean;
  search?: string;
  page?: number;
  per_page?: number;
  sort_by?: "name" | "created_at" | "updated_at";
  order?: "ASC" | "DESC";
}

export interface PantryUpdateData {
  name?: string;
  description?: string | null;
  sharedUserIds?: number[];
  metadata?: Record<string, any>;
}

export function isValidPantryData(body: any): { isValid: boolean; message?: string } {
  if (!body || typeof body !== "object") {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.REQUIRED("body") };
  }
  if (!body.name || typeof body.name !== "string") {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("name") };
  }
  if (body.description !== undefined && body.description !== null && typeof body.description !== "string") {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("description") };
  }
  if (body.shared_user_ids !== undefined && !Array.isArray(body.shared_user_ids)) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("shared_user_ids") };
  }
  if (body.metadata !== undefined && body.metadata !== null && typeof body.metadata !== "object") {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("metadata") };
  }
  return { isValid: true };
}

export function isValidPantryId(params: any): { isValid: boolean; message?: string } {
  if (!params.id || params.id === '') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("ID") };
  }
  
  const id = parseInt(params.id, 10);
  if (isNaN(id) || id < 0) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID };
  }
  return { isValid: true };
}

export function isValidUserId(params: any): { isValid: boolean; message?: string } {
  if (!params.user_id || params.user_id === '') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("User ID") };
  }
  
  const id = parseInt(params.user_id, 10);
  if (isNaN(id) || id < 0) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID_WITH_TYPE("User") };
  }
  return { isValid: true };
}
