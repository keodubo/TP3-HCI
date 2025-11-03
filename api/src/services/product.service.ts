import AppDataSource from "../db";
import { Product } from "../entities/product";
import { Category } from "../entities/category";
import { User } from "../entities/user";
import { NotFoundError, BadRequestError, ConflictError, handleCaughtError } from "../types/errors";
import { ERROR_MESSAGES } from '../types/errorMessages';
import {
  generateProductsFilteringOptions,
  GetProductsData,
  ProductUpdateData, RegisterProductData
} from "../types/product";
import { PaginatedResponse, createPaginationResponse } from '../types/pagination';

/**
* Retrieves user's products.
*
* @param {GetProductsData} productData - Filtering and pagination options
* @returns {Promise<PaginatedResponse<Product>>} Product information with pagination
* @throws {NotFoundError} If no products are found
*/
export async function getProductsService(productData: GetProductsData): Promise<PaginatedResponse<any>> {
  try {
    if (productData.category_id) {
      const category = await Category.findOne({ where: { id: productData.category_id, deletedAt: null } });
      if (!category) {
        throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.CATEGORY);
      }
    }

    const whereOptions = generateProductsFilteringOptions(productData);

    const orderDirection = productData.order && String(productData.order).toUpperCase() === "ASC" ? "ASC" : "DESC";
    let order: any = { name: orderDirection };
    if (productData.sort_by) {
      switch (productData.sort_by) {
        case "created_at":
          order = { createdAt: orderDirection };
          break;
        case "updated_at":
          order = { updatedAt: orderDirection };
          break;
        case "name":
        default:
          order = { name: orderDirection };
          break;
      }
    }

    const perPage = productData.per_page && productData.per_page > 0 ? productData.per_page : 10;
    const page = productData.page && productData.page > 0 ? productData.page : 1;

    const total = await Product.count({ where: whereOptions });

    const products: Product[] = await Product.find({
      where: whereOptions,
      relations: ["owner", "category"],
      order,
      take: perPage,
      skip: (page - 1) * perPage,
    });

    const formattedProducts = products.map((p) => p.getFormattedProduct());

    return createPaginationResponse(
      formattedProducts,
      total,
      page,
      perPage
    );
  } catch (err: unknown) {
    handleCaughtError(err);
  }
}

/**
 * Retrieves a product by its ID for a specific user.
 *
 * @param {number} id - Product ID
 * @param {User} owner - Authenticated user
 * @returns {Promise<Product>} Product information
 * @throws {NotFoundError} If product is not found
 */
export async function getProductByIdService(id: number, owner: User): Promise<Product> {
  try {
    const product = await Product.findOne({
      where: { id, owner: { id: owner.id }, deletedAt: null },
      relations: ["category"],
    });
    if (!product) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PRODUCT);
    return product.getFormattedProduct();
  } catch (err) {
    handleCaughtError(err);
  }
}

/**
 * Creates a new product for a user.
 * Runs inside a transaction to avoid race conditions.
 *
 * @param {RegisterProductData} data - Product creation data
 * @returns {Promise<Product>} Created product
 * @throws {BadRequestError} If category is not found
 */
export async function createProductService(data: RegisterProductData): Promise<Product> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();
  try {
    let category = null;
    if (data.categoryId !== undefined && data.categoryId !== null) {
      category = await queryRunner.manager.findOne(Category, { where: { id: data.categoryId, deletedAt: null } });
      if (!category) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.CATEGORY);
    }
    const product = new Product();
    product.name = data.name;
    product.description = data.description ?? null;
    product.category = category;
    product.unit = data.unit ?? null;
    product.defaultQuantity = data.defaultQuantity !== undefined ? data.defaultQuantity : 1;
    product.isFavorite = false;
    product.metadata = data.metadata ?? null;
    product.owner = data.owner;
    await queryRunner.manager.save(product);
    await queryRunner.commitTransaction();
    const savedProduct = await Product.findOne({ where: { id: product.id }, relations: ["category"] });
    return (savedProduct ?? product).getFormattedProduct();
  } catch (err: any) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    
    if (err.code === 'SQLITE_CONSTRAINT' || err.code === 'SQLITE_CONSTRAINT_UNIQUE' || err.code === '23505') {
      throw new ConflictError(ERROR_MESSAGES.CONFLICT.PRODUCT_EXISTS);
    }
    
    handleCaughtError(err);
  } finally {
    await queryRunner.release();
  }
}

/**
 * Updates an existing product for a user.
 * Runs inside a transaction to avoid race conditions.
 *
 * @param {number} id - Product ID
 * @param {User} owner - Authenticated user
 * @param {ProductUpdateData} data - Product update data
 * @returns {Promise<Product>} Updated product
 * @throws {NotFoundError} If product is not found
 * @throws {BadRequestError} If category is not found
 */
export async function updateProductService(id: number, owner: User, data: ProductUpdateData): Promise<Product> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();
  try {
    const product = await queryRunner.manager.findOne(Product, { where: { id, owner: { id: owner.id }, deletedAt: null }, relations: ["category"] });
    if (!product) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PRODUCT);
    
    if (data.name !== undefined) {
      product.name = data.name;
    }

    if (data.description !== undefined) {
      product.description = data.description;
    }

    if (data.categoryId !== undefined) {
      if (data.categoryId === null) {
        product.category = null;
      } else {
        const category = await queryRunner.manager.findOne(Category, { where: { id: data.categoryId, deletedAt: null } });
        if (!category) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.CATEGORY);
        product.category = category;
      }
    }

    if (data.unit !== undefined) {
      product.unit = data.unit;
    }

    if (data.defaultQuantity !== undefined) {
      product.defaultQuantity = data.defaultQuantity;
    }

    if (data.metadata !== undefined) {
      product.metadata = data.metadata;
    }
    await queryRunner.manager.save(product);
    await queryRunner.commitTransaction();
    const savedProduct = await Product.findOne({ where: { id: product.id }, relations: ["category"] });
    return (savedProduct ?? product).getFormattedProduct();
  } catch (err: any) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    
    if (err.code === 'SQLITE_CONSTRAINT' || err.code === 'SQLITE_CONSTRAINT_UNIQUE' || err.code === '23505') {
      throw new ConflictError(ERROR_MESSAGES.CONFLICT.PRODUCT_EXISTS);
    }
    
    handleCaughtError(err);
  } finally {
    await queryRunner.release();
  }
}

/**
 * Deletes a product for a user.
 * Runs inside a transaction to avoid race conditions.
 *
 * @param {number} id - Product ID
 * @param {User} owner - Authenticated user
 * @returns {Promise<boolean>} True if deleted
 * @throws {NotFoundError} If product is not found
 */
export async function deleteProductService(id: number, owner: User): Promise<boolean> {
  const queryRunner = AppDataSource.createQueryRunner();
  await queryRunner.connect();
  await queryRunner.startTransaction();
  try {
    const product = await queryRunner.manager.findOne(Product,
        { where: { id, owner: { id: owner.id }, deletedAt: null } });
    if (!product) throw new NotFoundError(ERROR_MESSAGES.NOT_FOUND.PRODUCT);
    await queryRunner.manager.softRemove(product);
    await queryRunner.commitTransaction();
    return true;
  } catch (err) {
    if (queryRunner.isTransactionActive) {
      await queryRunner.rollbackTransaction();
    }
    handleCaughtError(err);
    throw err;
  } finally {
    await queryRunner.release();
  }
}
