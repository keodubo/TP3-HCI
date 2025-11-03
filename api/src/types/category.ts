import {User} from "../entities/user";
import { Like } from "typeorm";
import { ERROR_MESSAGES } from './errorMessages';

export type RegisterCategoryData = {
  name: string;
  description?: string | null;
  owner: User;
  metadata?: Record<string, any>;
}

export type GetCategoryData = {
  name?: string;
  owner: User;
  per_page?: number;
  page?: string;
  sort_by?: 'name' | 'createdAt' | 'updatedAt';
  order?: 'ASC' | 'DESC';
}

export function isValidCategoryData(data: any): { isValid: boolean; message?: string } {
  if (!data || typeof data !== 'object') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.REQUIRED("body") };
  }

  if (!('name' in data) || typeof data.name !== 'string' || data.name.trim().length <= 0) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("name") };
  }

  if ('description' in data && data.description !== null && typeof data.description !== 'string') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("description") };
  }

  if ('metadata' in data && data.metadata !== undefined && typeof data.metadata !== 'object') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("metadata") };
  }

  return { isValid: true };
}

export function isValidCategoryId(data: any): { isValid: boolean; message?: string } {
  if (!data.id || data.id === undefined || data.id === null || data.id === '') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("ID") };
  }
  
  const id = parseInt(data.id, 10);
  if (isNaN(id) || id < 0) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID };
  }

  return { isValid: true };
}

export function generateCategoriesFilteringOptions(categoryData: GetCategoryData) {
  let whereOptions: any = {
    owner: { id: categoryData.owner.id },
  };

  if (categoryData.name) {
    whereOptions = { ...whereOptions, name: Like(`%${categoryData.name}%`) };
  }

  whereOptions.deletedAt = null;

  return whereOptions;
}
