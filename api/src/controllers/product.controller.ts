import { Request, Response } from "express";
import { replyCreated, replySuccess, replyWithError } from '../http';
import * as ProductService from '../services/product.service';
import {BadRequestError, ServerError} from '../types/errors';
import { User } from "../entities/user";
import {isValidProductData, isValidProductId, RegisterProductData} from "../types/product";
import { ERROR_MESSAGES } from '../types/errorMessages';

export async function getProducts(req: Request, res: Response): Promise<void> {
  try {
    const owner = req.user as User;
  const page: number = req.query.page ? Number(req.query.page) : 1;
  const per_page: number = req.query.per_page ? Number(req.query.per_page) : 10;
  const sortQuery = typeof req.query.sort_by === "string" ? req.query.sort_by : undefined;
  const sort_by = getSortByValue(sortQuery);
    const order: "ASC" | "DESC" = req.query.order
        ? String(req.query.order).toUpperCase() as "ASC" | "DESC"
        : "DESC";

    const search: string | undefined = req.query.search ? String(req.query.search) : undefined;
    const category_id: number | undefined = req.query.category_id !== undefined && req.query.category_id !== null
      ? Number(req.query.category_id)
      : undefined;

    const result = await ProductService.getProductsService({
      owner,
      search,
      category_id,
      page,
      per_page,
      sort_by,
      order,
    });

    replySuccess(res, result);
  } catch (err) {
    replyWithError(res, err);
  }
}

function getSortByValue(sortQuery: string | undefined): "name" | "created_at" | "updated_at" {
  switch (sortQuery) {
    case "created_at":
    case "updated_at":
      return sortQuery;
    default:
      return "name";
  }
}

export async function getProductById(req: Request, res: Response): Promise<void> {
  try {
    const owner = req.user as User;
    const validation = isValidProductId(req.params);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }
    const id = parseInt(req.params.id);
    const product = await ProductService.getProductByIdService(id, owner);
    replySuccess(res, product);
  } catch (err) {
    replyWithError(res, err);
  }
}

export async function createProduct(req: Request, res: Response): Promise<void> {
  try {
    const validation = isValidProductData(req.body);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }

    const productData: RegisterProductData = {
      name: req.body.name,
      description: req.body.description ?? null,
      owner: req.user as User,
      categoryId: req.body.category_id ? Number(req.body.category_id) : null,
      unit: req.body.unit ?? null,
      defaultQuantity: req.body.default_quantity !== undefined ? Number(req.body.default_quantity) : undefined,
      metadata: req.body.metadata,
    };

    replyCreated(res, await ProductService.createProductService(productData));
  } catch (err) {
    replyWithError(res, err);
  }
}

export async function updateProduct(req: Request, res: Response): Promise<void> {
  try {
    const owner = req.user as User;
    const idValidation = isValidProductId(req.params);
    if (!idValidation.isValid) {
      throw new BadRequestError(idValidation.message);
    }
    const id = Number(req.params.id);
    
    const bodyValidation = isValidProductData(req.body);
    if (!bodyValidation.isValid) {
      throw new BadRequestError(bodyValidation.message);
    }
    
    const product = await ProductService.updateProductService(id, owner, {
      name: req.body.name,
      description: req.body.description ?? null,
      categoryId: req.body.category_id !== undefined ? Number(req.body.category_id) : undefined,
      unit: req.body.unit ?? null,
      defaultQuantity: req.body.default_quantity !== undefined ? Number(req.body.default_quantity) : undefined,
      metadata: req.body.metadata,
    });
    replySuccess(res, product);
  } catch (err) {
    replyWithError(res, err);
  }
}

export async function deleteProduct(req: Request, res: Response): Promise<void> {
  try {
    const owner = req.user as User;
    const validation = isValidProductId(req.params);
    if (!validation.isValid) {
      throw new BadRequestError(validation.message);
    }
    const id = Number(req.params.id);

    const deleted = await ProductService.deleteProductService(id, owner)
    if (!deleted) throw new ServerError(ERROR_MESSAGES.SERVER.OPERATION_FAILED);

    replySuccess(res,{});
  } catch (err) {
    replyWithError(res, err);
  }
}
