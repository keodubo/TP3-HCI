import { ERROR_MESSAGES } from './errorMessages';

export type RegisterUserData = {
  email: string;
  password: string;
  displayName: string;
  metadata?: Record<string, any>;
}

export type LoginUserData = {
  email: string;
  password: string;
}

export type VerificationData = {
  email: string;
  code: string;
}

export type SendVerificationData = {
  email: string;
}

export type PasswordRecoveryData = {
  email: string;
}

export type PasswordResetData = {
  email: string;
  resetToken: string;
  password: string;
}

export type ChangePasswordData = {
  currentPassword: string;
  newPassword: string;
}

export type UpdateProfileData = {
  displayName?: string;
  phoneNumber?: string | null;
  preferredLanguage?: string | null;
  notificationOptIn?: boolean;
  themeMode?: string | null;
  metadata?: Record<string, any>;
  bio?: string | null;
}

export function isValidUserId(data: any): { isValid: boolean; message?: string } {
  if (!data.userId || data.userId === undefined || data.userId === null || data.userId === '') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("ID") };
  }
  
  const id = parseInt(data.userId, 10);
  if (isNaN(id) || id < 0) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_ID };
  }

  return { isValid: true };
}

export function isValidEmail(email: string): { isValid: boolean, message?: string } {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    return {
      isValid: false,
      message: ERROR_MESSAGES.VALIDATION.INVALID_EMAIL
    };
  }
  return { isValid: true };
}

export function isValidRegistrationData(data: RegisterUserData): { isValid: boolean; message?: string } {
  if (!data.email) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("email") };
  }

  const emailValidation = isValidEmail(data.email);
  if (!emailValidation.isValid) {
    return { isValid: false, message: emailValidation.message };
  }

  if (!data.password) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("password") };
  }

  if (data.password.length < 6) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_PASSWORD };
  }

  if (!data.displayName || data.displayName.trim().length === 0) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("display_name") };
  }

  return { isValid: true };
}

export function isValidLoginData(data: LoginUserData): { isValid: boolean; message?: string } {
  if (!data.email) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("email") };
  }

  if (!data.password) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("password") };
  }

  return { isValid: true };
}

export function isValidVerificationTokenData(data: VerificationData): { isValid: boolean; message?: string } {
  if (!data.email) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("email") };
  }
  if (!data.code) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("code") };
  }

  return { isValid: true };
}

export function isValidSendVerificationData(data: SendVerificationData): { isValid: boolean; message?: string } {
  if (!data.email) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("email") };
  }
  return { isValid: true };
}

export function isValidPasswordRecoveryData(data: PasswordRecoveryData): { isValid: boolean; message?: string } {
  if (!data.email) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("email") };
  }
  return { isValid: true };
}

export function isValidPasswordResetData(data: PasswordResetData): { isValid: boolean; message?: string } {
  if (!data.email) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("email") };
  }
  if (!data.resetToken) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("reset_token") };
  }
  if (!data.password) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.MISSING_FIELD("password") };
  }
  if (data.password.length < 6) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_PASSWORD };
  }

  return { isValid: true };
}

export function isValidChangePassword(data: ChangePasswordData): { isValid: boolean; message?: string } {
  if (!data.currentPassword || data.currentPassword.length < 6) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_PASSWORD };
  }

  if (!data.newPassword || data.newPassword.length < 6) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID_PASSWORD };
  }

  return { isValid: true };
}

export function isValidModificationData(data: UpdateProfileData): { isValid: boolean; message?: string } {
  if (!data) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.REQUIRED("body") };
  }

  if (data.displayName !== undefined && (typeof data.displayName !== 'string' || data.displayName.trim().length === 0)) {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("display_name") };
  }

  if (data.themeMode !== undefined && typeof data.themeMode !== 'string') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("theme_mode") };
  }

  if (data.metadata !== undefined && typeof data.metadata !== 'object') {
    return { isValid: false, message: ERROR_MESSAGES.VALIDATION.INVALID("metadata") };
  }

  return { isValid: true };
}
