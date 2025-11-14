package com.comprartir.mobile.products.data

import android.util.Log
import com.comprartir.mobile.core.data.mapper.toEntity
import com.comprartir.mobile.core.database.dao.CategoryDao
import com.comprartir.mobile.core.database.dao.ProductDao
import com.comprartir.mobile.core.database.entity.CategoryEntity
import com.comprartir.mobile.core.database.entity.ProductEntity
import com.comprartir.mobile.core.network.CategoryUpsertRequest
import com.comprartir.mobile.core.network.ComprartirApi
import com.comprartir.mobile.core.network.ProductUpsertRequest
import com.comprartir.mobile.core.network.fetchAllPages
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

data class Product(
    val id: String,
    val name: String,
    val description: String?,
    val category: Category?,
    val unit: String?,
    val defaultQuantity: Double,
    val isFavorite: Boolean,
)

data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val color: String?,
    val iconName: String? = null,
)

interface ProductsRepository {
    fun observeCatalog(): Flow<List<Product>>
    fun observeCategories(): Flow<List<Category>>
    suspend fun refresh()
    suspend fun createCategory(name: String, description: String? = null): Category
    suspend fun updateCategory(categoryId: String, name: String, description: String?)
    suspend fun deleteCategory(categoryId: String)
    suspend fun upsertProduct(product: Product): Product
    suspend fun deleteProduct(productId: String)
    suspend fun assignCategory(productId: String, categoryId: String)
}

@Singleton
class DefaultProductsRepository @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val api: ComprartirApi,
) : ProductsRepository {

    private val hasSyncedCatalog = AtomicBoolean(false)
    private val hasSyncedCategories = AtomicBoolean(false)

    override fun observeCatalog(): Flow<List<Product>> = combine(
        productDao.observeProducts(),
        categoryDao.observeCategories(),
    ) { productEntities, categoryEntities ->
        val categoryMap = categoryEntities.associateBy { it.id }
        productEntities.map { entity ->
            entity.toDomainModel(categoryMap[entity.categoryId]?.toDomainModel())
        }
    }.onStart {
        ensureCatalogSynced()
    }

    override fun observeCategories(): Flow<List<Category>> = categoryDao.observeCategories()
        .map { categories -> categories.map { it.toDomainModel() } }
        .onStart { ensureCategoriesSynced() }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            refreshCategoriesInternal()
            refreshProductsInternal()
        }
    }

    override suspend fun createCategory(name: String, description: String?): Category {
        return withContext(Dispatchers.IO) {
            val created = api.createCategory(CategoryUpsertRequest(name = name, description = description))
            val entity = created.toEntity()
            categoryDao.upsertAll(listOf(entity))
            entity.toDomainModel()
        }
    }

    override suspend fun updateCategory(categoryId: String, name: String, description: String?) {
        withContext(Dispatchers.IO) {
            api.updateCategory(categoryId, CategoryUpsertRequest(name = name, description = description))
            refreshCategoriesInternal()
        }
    }

    override suspend fun deleteCategory(categoryId: String) {
        withContext(Dispatchers.IO) {
            api.deleteCategory(categoryId)
            refreshCategoriesInternal()
        }
    }

    override suspend fun upsertProduct(product: Product): Product = withContext(Dispatchers.IO) {
        val categoryRef = product.category?.id?.toIntOrNull()?.let { 
            com.comprartir.mobile.core.network.CategoryRef(id = it) 
        }
        
        val request = com.comprartir.mobile.core.network.ProductUpsertRequest(
            name = product.name,
            category = categoryRef,
            metadata = null,
        )
        
        val response = if (product.id.isBlank()) {
            try {
                Log.d(TAG, "Creating new product: ${product.name}")
                api.createProduct(request)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 409) {
                    // Product already exists, refresh and find it
                    Log.d(TAG, "Product '${product.name}' already exists (409), searching for it...")
                    refreshProductsInternal()
                    val existing = productDao.getByName(product.name)
                    if (existing != null) {
                        Log.d(TAG, "Found existing product with id=${existing.id}")
                        // Return the existing product DTO
                        api.getProduct(existing.id)
                    } else {
                        // Fallback: search via API
                        Log.d(TAG, "Searching product via API...")
                        val products = api.getProducts(page = 1, perPage = 50)
                        val found = products.data.firstOrNull { it.name.equals(product.name, ignoreCase = true) }
                        if (found != null) {
                            Log.d(TAG, "Found product via API: id=${found.id}")
                            found
                        } else {
                            throw Exception("El producto '${product.name}' ya existe pero no se pudo encontrar.")
                        }
                    }
                } else {
                    Log.e(TAG, "HTTP ${e.code()} error creating product", e)
                    throw Exception("Error al crear producto: ${e.message()}")
                }
            }
        } else {
            Log.d(TAG, "Updating product: ${product.id}")
            api.updateProduct(product.id, request)
        }
        
        productDao.upsert(response.toEntity())
        Log.d(TAG, "Product saved to local DB: id=${response.id}, name=${response.name}")
        
        // Return the domain Product with valid ID
        Product(
            id = response.id,
            name = response.name,
            description = response.description,
            category = response.category?.let { Category(id = it.id, name = it.name, description = it.description, color = null) },
            unit = response.unit,
            defaultQuantity = response.defaultQuantity,
            isFavorite = response.isFavorite,
        )
    }

    override suspend fun deleteProduct(productId: String) {
        withContext(Dispatchers.IO) {
            try {
                api.deleteProduct(productId)
            } catch (throwable: Throwable) {
                Log.w(TAG, "Failed to delete product from API", throwable)
            }
            productDao.delete(productId)
        }
    }

    override suspend fun assignCategory(productId: String, categoryId: String) {
        withContext(Dispatchers.IO) {
            val existing = productDao.getById(productId) ?: return@withContext
            
            val categoryRef = categoryId.toIntOrNull()?.let { 
                com.comprartir.mobile.core.network.CategoryRef(id = it) 
            }
            
            val request = com.comprartir.mobile.core.network.ProductUpsertRequest(
                name = existing.name,
                category = categoryRef,
                metadata = null,
            )
            val updated = api.updateProduct(productId, request)
            productDao.upsert(updated.toEntity())
        }
    }

    private suspend fun ensureCatalogSynced() {
        if (hasSyncedCatalog.compareAndSet(false, true)) {
            withContext(Dispatchers.IO) {
                refreshCategoriesInternal()
                refreshProductsInternal()
            }
        }
    }

    private suspend fun ensureCategoriesSynced() {
        if (hasSyncedCategories.compareAndSet(false, true)) {
            withContext(Dispatchers.IO) {
                refreshCategoriesInternal()
            }
        }
    }

    private suspend fun refreshCategoriesInternal() {
        try {
            val categories = fetchAllPages { page, perPage -> api.getCategories(page = page, perPage = perPage) }
            categoryDao.clearAll()
            categoryDao.upsertAll(categories.map { it.toEntity() })
        } catch (throwable: Throwable) {
            Log.w(TAG, "Failed to refresh categories", throwable)
        }
    }

    private suspend fun refreshProductsInternal() {
        try {
            val products = fetchAllPages { page, perPage -> api.getProducts(page = page, perPage = perPage) }
            productDao.clearAll()
            productDao.upsertAll(products.map { it.toEntity() })
        } catch (throwable: Throwable) {
            Log.w(TAG, "Failed to refresh products", throwable)
        }
    }

    private fun ProductEntity.toDomainModel(category: Category?): Product = Product(
        id = id,
        name = name,
        description = description,
        category = category,
        unit = unit,
        defaultQuantity = defaultQuantity,
        isFavorite = isFavorite,
    )

    private fun CategoryEntity.toDomainModel(): Category = Category(
        id = id,
        name = name,
        description = description,
        color = color,
        iconName = iconName,
    )

    companion object {
        private const val TAG = "ProductsRepository"
    }
}
