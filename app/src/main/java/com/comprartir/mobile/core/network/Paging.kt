package com.comprartir.mobile.core.network

/**
 * Helper to exhaust paginated endpoints that follow the Comprartir `page`/`per_page` contract.
 */
suspend fun <T> fetchAllPages(
    pageSize: Int = DEFAULT_PAGE_SIZE,
    fetch: suspend (page: Int, perPage: Int) -> ApiListResponse<T>,
): List<T> {
    val allItems = mutableListOf<T>()
    var page = 1
    var hasMore: Boolean
    do {
        val response = fetch(page, pageSize)
        allItems += response.data
        val total = response.total
        hasMore = when {
            total != null -> allItems.size < total
            response.data.size < pageSize -> false
            else -> true
        }
        page++
    } while (hasMore)
    return allItems
}

private const val DEFAULT_PAGE_SIZE = 50
