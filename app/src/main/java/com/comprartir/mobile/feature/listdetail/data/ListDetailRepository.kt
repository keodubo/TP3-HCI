package com.comprartir.mobile.feature.listdetail.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlin.random.Random

interface ListDetailRepository {
    fun observeList(listId: String): Flow<ListDetailData>
    suspend fun toggleItem(listId: String, itemId: String, completed: Boolean)
    suspend fun deleteItem(listId: String, itemId: String): ListDetailItem?
    suspend fun restoreItem(listId: String, item: ListDetailItem)
    suspend fun addItem(listId: String, name: String, quantity: String, unit: String?): ListDetailItem
}

data class ListDetailData(
    val id: String,
    val title: String,
    val subtitle: String,
    val shareLink: String,
    val items: List<ListDetailItem>,
)

data class ListDetailItem(
    val id: String,
    val name: String,
    val quantity: String,
    val unit: String?,
    val notes: String?,
    val isCompleted: Boolean,
)

@Singleton
class FakeListDetailRepository @Inject constructor() : ListDetailRepository {

    private val lists = mutableMapOf<String, MutableStateFlow<ListDetailData>>()

    override fun observeList(listId: String): Flow<ListDetailData> = ensureList(listId)
        .onStart { delay(400) }

    override suspend fun toggleItem(listId: String, itemId: String, completed: Boolean) {
        delay(120)
        ensureList(listId).update { current ->
            current.copy(
                items = current.items.map { item ->
                    if (item.id == itemId) {
                        item.copy(isCompleted = completed)
                    } else item
                },
            )
        }
    }

    override suspend fun deleteItem(listId: String, itemId: String): ListDetailItem? {
        delay(150)
        var removed: ListDetailItem? = null
        ensureList(listId).update { current ->
            val remaining = current.items.filterNot {
                val shouldRemove = it.id == itemId
                if (shouldRemove) {
                    removed = it
                }
                shouldRemove
            }
            current.copy(items = remaining)
        }
        return removed
    }

    override suspend fun restoreItem(listId: String, item: ListDetailItem) {
        delay(120)
        ensureList(listId).update { current ->
            current.copy(items = listOf(item) + current.items)
        }
    }

    override suspend fun addItem(listId: String, name: String, quantity: String, unit: String?): ListDetailItem {
        delay(200)
        val newItem = ListDetailItem(
            id = "item-${Random.nextInt(1_000, 9_999)}",
            name = name,
            quantity = quantity.ifBlank { "1" },
            unit = unit?.ifBlank { null },
            notes = null,
            isCompleted = false,
        )
        ensureList(listId).update { current ->
            current.copy(items = current.items + newItem)
        }
        return newItem
    }

    private fun ensureList(listId: String): MutableStateFlow<ListDetailData> {
        return lists.getOrPut(listId) {
            MutableStateFlow(
                ListDetailData(
                    id = listId,
                    title = "Lista #$listId",
                    subtitle = "Actualizada hace 2 h",
                    shareLink = "https://comprartir.app/lists/$listId",
                    items = emptyList(),
                )
            )
        }
    }
}

