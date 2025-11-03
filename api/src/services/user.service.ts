import AppDataSource from "../db";
import { User } from "../entities/user";
import {
  BadRequestError,
  ConflictError,
  NotFoundError,
  UnauthorizedError,
  handleCaughtError
} from "../types/errors";
import {
  ChangePasswordData,
  LoginUserData,
  PasswordRecoveryData,
  PasswordResetData,
  RegisterUserData,
  UpdateProfileData,
  VerificationData
} from "../types/user";
import { getHashedPassword, isValidPassword } from "../utils/passwords";
import { validate } from "class-validator";
import * as jwt from "jsonwebtoken";
import { EmailType, Mailer } from "./email.service";
import { UserVerificationToken } from "../entities/userVerificationToken";
import { generateUserToken } from "../utils/tokens";
import { UserPasswordRecoveryToken } from "../entities/userPasswordRecoveryToken";
import { removeUserPrivateValues } from "../utils/users";
import { QueryFailedError } from "typeorm";
import { ERROR_MESSAGES } from '../types/errorMessages';

const TOKEN_EXPIRATION = "30d";

function ensureJwtSecret(): string {
  if (!process.env.JWT_TOKEN) {
    throw new Error("Missing JWT_TOKEN environment variable");
  }
  return process.env.JWT_TOKEN;
}

function signAuthToken(userId: number): string {
  const secret = ensureJwtSecret();
  return jwt.sign({ sub: userId }, secret, { expiresIn: TOKEN_EXPIRATION });
}

function buildAuthResponse(user: User): { token: string; user: any } {
  removeUserPrivateValues(user);
  return {
    token: signAuthToken(user.id),
    user: user.getFormattedUser(),
  };
}

function mapValidationErrors(errors: any[]): string {
  return errors
    .map(error => Object.values(error.constraints || {}).join(", "))
    .filter(Boolean)
    .join(", ");
}

function normalizeEmail(email: string): string {
  return email.trim().toLowerCase();
}

export async function createNewUser(
  userData: RegisterUserData,
  mailer: Mailer
): Promise<{ token: string; user: any }> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();

  try {
    const user = new User();
    user.displayName = userData.displayName.trim();
    user.email = normalizeEmail(userData.email);
    user.password = getHashedPassword(userData.password);
    user.metadata = userData.metadata ?? {};

    const errors = await validate(user);
    if (errors.length > 0) {
      throw new BadRequestError(mapValidationErrors(errors));
    }

    await queryRunner.manager.save(user);

    const verificationToken = new UserVerificationToken();
    verificationToken.expirationDate = new Date(Date.now() + 24 * 60 * 60 * 1000);
    verificationToken.token = generateUserToken();
    verificationToken.user = user;
    await queryRunner.manager.save(verificationToken);

    await queryRunner.commitTransaction();

    await mailer.sendEmail(EmailType.REGISTRATION, user.displayName, verificationToken.token);

    const freshUser = await User.findOne({ where: { id: user.id } });
    if (!freshUser) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    return buildAuthResponse(freshUser);
  } catch (err: any) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }

    if (err instanceof QueryFailedError && `${err.message}`.includes('UNIQUE constraint failed: user.email')) {
      throw new ConflictError(ERROR_MESSAGES.BUSINESS_RULE.EMAIL_ALREADY_EXISTS);
    }

    throw err;
  } finally {
    await queryRunner.release();
  }
}

export async function authenticateUser(userData: LoginUserData): Promise<{ token: string; user: any }> {
  const user: User | null = await User.createQueryBuilder('user')
    .addSelect('user.password')
    .where('LOWER(user.email) = :email', { email: normalizeEmail(userData.email) })
    .getOne();

  if (!user || !isValidPassword(userData.password, user.password)) {
    throw new UnauthorizedError(ERROR_MESSAGES.AUTH.INVALID_CREDENTIALS);
  }

  if (!user.isVerified) {
    throw new UnauthorizedError(ERROR_MESSAGES.AUTH.ACCOUNT_NOT_VERIFIED);
  }

  return buildAuthResponse(user);
}

export async function getUserService(currentUser: User): Promise<any> {
  try {
    if (!currentUser) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    const user: User | null = await User.findOne({ where: { id: currentUser.id } });
    if (!user) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    return user.getProfile();
  } catch (err) {
    handleCaughtError(err);
    throw err;
  }
}

export async function verifyUser(data: VerificationData): Promise<{ token: string; user: any }> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();

  try {
    const userVerificationToken: UserVerificationToken | null =
      await queryRunner.manager.findOne(UserVerificationToken, {
        where: { token: data.code },
        relations: ["user"]
      });

    if (!userVerificationToken) {
      throw new BadRequestError(ERROR_MESSAGES.VALIDATION.INVALID("verification code"));
    }

    const user: User = userVerificationToken.user;
    if (normalizeEmail(user.email) !== normalizeEmail(data.email)) {
      throw new BadRequestError(ERROR_MESSAGES.VALIDATION.INVALID("verification code"));
    }

    if (user.isVerified) {
      throw new ConflictError(ERROR_MESSAGES.CONFLICT.ACCOUNT_ALREADY_VERIFIED);
    }

    if (userVerificationToken.expirationDate < new Date()) {
      throw new BadRequestError(ERROR_MESSAGES.VALIDATION.INVALID("verification code (expired)"));
    }

    user.isVerified = true;
    await queryRunner.manager.save(user);
    await queryRunner.manager.remove(userVerificationToken);

    await queryRunner.commitTransaction();

    const savedUser = await User.findOne({ where: { id: user.id } });
    if (!savedUser) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    return buildAuthResponse(savedUser);
  } catch (err) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    throw err;
  } finally {
    await queryRunner.release();
  }
}

export async function sendVerificationCode(email: string, mailer: Mailer): Promise<{ code: string }> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();

  try {
    const user: User | null = await queryRunner.manager.findOne(User, {
      where: { email: normalizeEmail(email) },
      relations: ["verificationToken"]
    });

    if (!user) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    if (user.isVerified) {
      throw new ConflictError(ERROR_MESSAGES.CONFLICT.ACCOUNT_ALREADY_VERIFIED);
    }

    if (user.verificationToken) {
      await queryRunner.manager.remove(user.verificationToken);
    }

    const verificationToken = new UserVerificationToken();
    verificationToken.token = generateUserToken();
    verificationToken.expirationDate = new Date(Date.now() + 24 * 60 * 60 * 1000);
    verificationToken.user = user;
    await queryRunner.manager.save(verificationToken);

    await queryRunner.commitTransaction();

    await mailer.sendEmail(EmailType.REGISTRATION, user.displayName, verificationToken.token);

    return { code: verificationToken.token };
  } catch (err) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    throw err;
  } finally {
    await queryRunner.release();
  }
}

export async function sendPasswordRecoveryEmail(email: string, mailer: Mailer): Promise<boolean> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();

  try {
    const user: User | null = await queryRunner.manager.findOne(User, {
      where: { email: normalizeEmail(email) },
      relations: ["passwordRecoveryToken"]
    });

    if (!user) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    const oldToken = user.passwordRecoveryToken;
    if (oldToken) {
      await queryRunner.manager.remove(oldToken);
    }

    const newToken: UserPasswordRecoveryToken = new UserPasswordRecoveryToken();
    newToken.user = user;
    newToken.token = generateUserToken();
    newToken.expirationDate = new Date(Date.now() + 24 * 60 * 60 * 1000);

    await queryRunner.manager.save(newToken);
    await queryRunner.commitTransaction();

    await mailer.sendEmail(EmailType.RESET_PASSWORD, newToken.token, newToken.expirationDate);

    return true;
  } catch (err) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    throw err;
  } finally {
    await queryRunner.release();
  }
}

export async function resetUserPassword(resetPasswordData: PasswordResetData): Promise<{ token: string; user: any }> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();

  try {
    const recoveryToken: UserPasswordRecoveryToken | null =
      await queryRunner.manager.findOne(UserPasswordRecoveryToken, {
        where: { token: resetPasswordData.resetToken },
        relations: ["user"]
      });

    if (!recoveryToken) {
      throw new BadRequestError(ERROR_MESSAGES.VALIDATION.INVALID("reset token"));
    }

    const user: User = recoveryToken.user;
    if (normalizeEmail(user.email) !== normalizeEmail(resetPasswordData.email)) {
      throw new BadRequestError(ERROR_MESSAGES.VALIDATION.INVALID("reset token"));
    }

    if (recoveryToken.expirationDate < new Date()) {
      throw new BadRequestError(ERROR_MESSAGES.VALIDATION.INVALID("reset token (expired)"));
    }

    user.password = getHashedPassword(resetPasswordData.password);
    await queryRunner.manager.save(user);
    await queryRunner.manager.remove(recoveryToken);

    await queryRunner.commitTransaction();

    const refreshedUser = await User.findOne({ where: { id: user.id } });
    if (!refreshedUser) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    return buildAuthResponse(refreshedUser);
  } catch (err) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    throw err;
  } finally {
    await queryRunner.release();
  }
}

export async function changePassword(userId: number, data: ChangePasswordData): Promise<void> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();

  try {
    const user: User | null = await queryRunner.manager.createQueryBuilder(User, 'user')
      .addSelect('user.password')
      .where('user.id = :id', { id: userId })
      .getOne();

    if (!user) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    if (!isValidPassword(data.currentPassword, user.password)) {
      throw new BadRequestError(ERROR_MESSAGES.AUTH.INVALID_CREDENTIALS);
    }

    user.password = getHashedPassword(data.newPassword);
    await queryRunner.manager.save(user);
    await queryRunner.commitTransaction();
  } catch (err) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    throw err;
  } finally {
    await queryRunner.release();
  }
}

export async function updateUserProfile(userId: number, data: UpdateProfileData): Promise<any> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();

  try {
    const user: User | null = await queryRunner.manager.findOne(User, { where: { id: userId } });
    if (!user) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    if (data.displayName !== undefined) {
      user.displayName = data.displayName.trim();
    }
    if (data.phoneNumber !== undefined) {
      user.phoneNumber = data.phoneNumber;
    }
    if (data.preferredLanguage !== undefined) {
      user.preferredLanguage = data.preferredLanguage;
    }
    if (data.notificationOptIn !== undefined) {
      user.notificationOptIn = data.notificationOptIn;
    }
    if (data.themeMode !== undefined) {
      user.themeMode = data.themeMode;
    }
    if (data.metadata !== undefined) {
      user.metadata = data.metadata;
    }
    if (data.bio !== undefined) {
      user.bio = data.bio;
    }

    const errors = await validate(user, { skipMissingProperties: true });
    if (errors.length > 0) {
      throw new BadRequestError(mapValidationErrors(errors));
    }

    await queryRunner.manager.save(user);
    await queryRunner.commitTransaction();

    const refreshedUser = await User.findOne({ where: { id: user.id } });
    if (!refreshedUser) {
      throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.USER);
    }

    return refreshedUser.getProfile();
  } catch (err) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    throw err;
  } finally {
    await queryRunner.release();
  }
}
