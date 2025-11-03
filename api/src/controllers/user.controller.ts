import { Request, Response } from "express";
import {replyCreated, replySuccess, replyWithError} from '../http';
import { Mailer } from '../services/email.service';
import * as UserService from '../services/user.service';
import { BadRequestError } from '../types/errors';
import {
  ChangePasswordData,
  LoginUserData,
  PasswordRecoveryData,
  PasswordResetData,
  RegisterUserData,
  SendVerificationData,
  UpdateProfileData,
  VerificationData,
  isValidChangePassword,
  isValidEmail,
  isValidLoginData,
  isValidModificationData,
  isValidPasswordRecoveryData,
  isValidPasswordResetData,
  isValidRegistrationData,
  isValidSendVerificationData,
  isValidVerificationTokenData,
} from '../types/user';
import { User } from "../entities/user";
import { ERROR_MESSAGES } from '../types/errorMessages';

export async function registerUser(req: Request, res: Response): Promise<void> {
  try {
    const userData: RegisterUserData = {
      email: req.body.email,
      password: req.body.password,
      displayName: req.body.display_name,
      metadata: req.body.metadata,
    };

    const validation = isValidRegistrationData(userData);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }

    const mailer: Mailer = req.app.locals.mailer;

    replyCreated(res, await UserService.createNewUser(userData, mailer));

  } catch (err) {
    replyWithError(res, err);
  }
}

export async function getUser(req: Request, res: Response): Promise<void> {
  try {
    replySuccess(res, await UserService.getUserService(req.user as User));
  } catch (err) {
    replyWithError(res, err);
  }
}


export async function loginUser(req: Request, res: Response): Promise<void> {
  try {
    const loginData: LoginUserData = {
      email: req.body.email,
      password: req.body.password,
    };
    const validation = isValidLoginData(loginData);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }

    const emailValidation = isValidEmail(loginData.email);
    if (!emailValidation.isValid) {
      throw new BadRequestError(emailValidation.message);
    }

    replySuccess(res, await UserService.authenticateUser(loginData));
  } catch (err) {
    replyWithError(res, err);
  }
}

export let tokenBlacklist = new Set();
export function logoutUser(req: Request, res: Response): void {
  try {
    const token = req.headers.authorization?.split(' ')[1];
    if (token) {
      tokenBlacklist.add(token);
    }

    replySuccess(res, {});

  } catch (err) {
    replyWithError(res, err);
  }
}

export async function verifyUser(req: Request, res: Response): Promise<void> {
  try {
    const payload: VerificationData = {
      email: req.body.email,
      code: req.body.code,
    };
    const validation = isValidVerificationTokenData(payload);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }

    replySuccess(res, await UserService.verifyUser(payload));
  } catch (err) {
    replyWithError(res, err);
  }
}

export async function sendVerificationCode(req: Request, res: Response): Promise<void> {
  try {
    const payload: SendVerificationData = {
      email: req.body.email,
    };
    const validation = isValidSendVerificationData(payload);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }

    const emailValidation = isValidEmail(payload.email);
    if (!emailValidation.isValid) {
      throw new BadRequestError(emailValidation.message);
    }

    const mailer: Mailer = req.app.locals.mailer;
    replySuccess(res, await UserService.sendVerificationCode(payload.email, mailer));
  } catch (err) {
    replyWithError(res, err);
  }
}

export async function sendPasswordRecoveryCode(req: Request, res: Response): Promise<void> {
  try {
    const payload: PasswordRecoveryData = {
      email: req.body.email,
    };
    const validation = isValidPasswordRecoveryData(payload);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }

    const emailValidation = isValidEmail(payload.email);
    if (!emailValidation.isValid) {
      throw new BadRequestError(emailValidation.message);
    }

    const mailer: Mailer = req.app.locals.mailer;
    await UserService.sendPasswordRecoveryEmail(payload.email, mailer)
    replySuccess(res, {});
  } catch (err) {
    replyWithError(res, err);
  }
}

export async function resetPassword(req: Request, res: Response): Promise<void> {
  try {
    const payload: PasswordResetData = {
      email: req.body.email,
      resetToken: req.body.reset_token,
      password: req.body.password,
    };
    const validation = isValidPasswordResetData(payload);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }
    const response = await UserService.resetUserPassword(payload);
    replySuccess(res, response);
  } catch (err) {
    replyWithError(res, err);
  }
}

export async function changePassword(req: Request, res: Response): Promise<void> {
  try {
    const userId = (req.user as User).id;
    const payload: ChangePasswordData = {
      currentPassword: req.body.current_password,
      newPassword: req.body.new_password,
    };
    const validation = isValidChangePassword(payload);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }
    
    await UserService.changePassword(userId, payload)
    replySuccess(res, {});
  } catch (err) {
    replyWithError(res, err);
  }
}

export async function updateUserProfile(req: Request, res: Response): Promise<void> {
  try {
    const userId = (req.user as User).id;
    const payload: UpdateProfileData = {
      displayName: req.body.display_name,
      phoneNumber: req.body.phone_number,
      preferredLanguage: req.body.preferred_language,
      notificationOptIn: req.body.notification_opt_in,
      themeMode: req.body.theme_mode,
      metadata: req.body.metadata,
      bio: req.body.bio,
    };

    const validation = isValidModificationData(payload);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }

    replySuccess(res, await UserService.updateUserProfile(userId, payload));
  } catch (err) {
    replyWithError(res, err);
  }
}
