package com.comprartir.mobile.feature.lists.data

import javax.inject.Inject
import javax.inject.Singleton
import com.comprartir.mobile.feature.lists.model.ListTypeFilter
import com.comprartir.mobile.feature.lists.model.ListsSummaryUi
import com.comprartir.mobile.feature.lists.model.ShoppingListUi
import com.comprartir.mobile.feature.lists.model.SortDirection
import com.comprartir.mobile.feature.lists.model.SortOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlin.random.Random

interface ListsRepository {
    fun getLists(): Flow<List<ShoppingListUi>>
    fun getSummary(): Flow<ListsSummaryUi>
    suspend fun searchLists(
        query: String,
        sortOption: SortOption,
        direction: SortDirection,
        typeFilter: ListTypeFilter,
    ): List<ShoppingListUi>
    suspend fun createList(
        name: String,
        description: String,
        isRecurring: Boolean,
    ): String
}

@Singleton
class FakeListsRepository @Inject constructor() : ListsRepository {
    // TODO: Replace with real repository once network + database layer is ready.
    private val sampleLists = listOf(
        sampleList("1", "Compra semanal", updatedMinutesAgo = 90, total = 24, acquired = 12, shared = 3, isShared = true),
        sampleList("2", "Desayuno saludable", updatedMinutesAgo = 15, total = 12, acquired = 5, shared = 0, isShared = false),
        sampleList("3", "Fiesta sábado", updatedMinutesAgo = 240, total = 30, acquired = 6, shared = 4, isShared = true),
        sampleList("4", "Mudanza Lola", updatedMinutesAgo = 480, total = 18, acquired = 10, shared = 2, isShared = true),
        sampleList("5", "Despensa mensual", updatedMinutesAgo = 60, total = 40, acquired = 35, shared = 0, isShared = false),
    )

    private val listsFlow = MutableStateFlow(sampleLists)

    override fun getLists(): Flow<List<ShoppingListUi>> = listsFlow
        .onStart { delay(400) }

    override fun getSummary(): Flow<ListsSummaryUi> = listsFlow
        .map { lists ->
            ListsSummaryUi(
                sharedCount = lists.count { it.isShared },
                pendingItems = lists.sumOf { it.totalItems - it.acquiredItems },
                recurringReminders = lists.count { it.isShared.not() && it.totalItems > 20 },
            )
        }
        .onStart { delay(250) }

    override suspend fun searchLists(
        query: String,
        sortOption: SortOption,
        direction: SortDirection,
        typeFilter: ListTypeFilter,
    ): List<ShoppingListUi> {
        delay(300)
        var result = listsFlow.value
        if (query.isNotBlank()) {
            result = result.filter { it.name.contains(query.trim(), ignoreCase = true) }
        }
        result = when (typeFilter) {
            ListTypeFilter.ALL -> result
            ListTypeFilter.SHARED -> result.filter { it.isShared }
            ListTypeFilter.PERSONAL -> result.filterNot { it.isShared }
        }
        result = when (sortOption) {
            SortOption.NAME -> result.sortedBy { it.name.lowercase() }
            SortOption.RECENT -> result // sample data already roughly sorted
            SortOption.PROGRESS -> result.sortedBy { progressFraction(it) }
        }
        if (direction == SortDirection.DESCENDING) {
            result = result.reversed()
        }
        return result
    }

    override suspend fun createList(
        name: String,
        description: String,
        isRecurring: Boolean,
    ): String {
        delay(350)
        val newId = Random.nextInt(100, 999).toString()
        val newList = ShoppingListUi(
            id = newId,
            name = name.ifBlank { "Lista $newId" },
            updatedAgo = "Hace 1m",
            totalItems = 0,
            acquiredItems = 0,
            sharedWith = if (isRecurring) 0 else Random.nextInt(0, 4),
            isShared = !isRecurring && Random.nextBoolean(),
        )
        listsFlow.update { current ->
            listOf(newList) + current
        }
        return newList.id
    }

    private fun progressFraction(list: ShoppingListUi): Double =
        if (list.totalItems == 0) 0.0 else list.acquiredItems.toDouble() / list.totalItems.toDouble()

    private fun sampleList(
        id: String,
        name: String,
        updatedMinutesAgo: Long,
        total: Int,
        acquired: Int,
        shared: Int,
        isShared: Boolean,
    ): ShoppingListUi {
        val updatedAgo = formatRelativeTime(updatedMinutesAgo)
        return ShoppingListUi(
            id = id,
            name = name,
            updatedAgo = updatedAgo,
            totalItems = total,
            acquiredItems = acquired,
            sharedWith = shared,
            isShared = isShared,
        )
    }

    private fun formatRelativeTime(minutesAgo: Long): String {
        return when {
            minutesAgo < 60 -> "Hace ${minutesAgo}m"
            minutesAgo < 120 -> "Hace 1h"
            minutesAgo < 24 * 60 -> "Hace ${minutesAgo / 60}h"
            else -> "Hace ${minutesAgo / (60 * 24)}d"
        }
    }
}
