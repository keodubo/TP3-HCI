package com.comprartir.mobile.feature.home.domain

import android.content.Context
import android.text.format.DateUtils
import com.comprartir.mobile.R
import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.feature.home.model.ListStatusType
import com.comprartir.mobile.feature.home.model.RecentListUi
import com.comprartir.mobile.feature.home.model.SharedListUi
import com.comprartir.mobile.lists.data.ShoppingList
import com.comprartir.mobile.lists.data.ShoppingListsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

data class HomeListsData(
    val recentLists: List<RecentListUi>,
    val sharedLists: List<SharedListUi>,
)

/**
 * Use case that transforms ShoppingList domain models into UI models for Home screen.
 * Separates lists into:
 * - Recent lists: User's own lists, sorted by update time
 * - Shared lists: Lists shared with the user (where they're not the owner)
 */
class GetHomeListsUseCase @Inject constructor(
    private val listsRepository: ShoppingListsRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) {

    operator fun invoke(): Flow<HomeListsData> = combine(
        listsRepository.observeLists(),
        authRepository.currentUser,
    ) { allLists, currentUser ->
        val userId = currentUser?.id.orEmpty()
        val now = Instant.now()
        
        android.util.Log.d("GetHomeListsUseCase", "invoke: userId=$userId, allLists.size=${allLists.size}")
        android.util.Log.d("GetHomeListsUseCase", "invoke: List IDs = ${allLists.map { it.id }}")
        android.util.Log.d("GetHomeListsUseCase", "invoke: OwnerIds = ${allLists.map { "${it.id}:${it.ownerId}" }}")

        // Separate own lists from shared ones
        val ownLists = allLists.filter { it.ownerId == userId }
        val sharedLists = allLists.filter { it.ownerId != userId && it.sharedWith.contains(userId) }
        
        android.util.Log.d("GetHomeListsUseCase", "invoke: ownLists=${ownLists.size}, sharedLists=${sharedLists.size}")

        // Map to UI models
        val recentListsUi = ownLists
            .sortedByDescending { it.updatedAt }
            .take(5)
            .map { it.toRecentListUi(now) }

        val sharedListsUi = sharedLists
            .sortedByDescending { it.updatedAt }
            .take(5)
            .map { it.toSharedListUi(now) }

        HomeListsData(
            recentLists = recentListsUi,
            sharedLists = sharedListsUi,
        )
    }

    /**
     * Triggers an explicit refresh from the backend.
     * The repository will automatically update its flow when data changes.
     */
    suspend fun refresh() {
        listsRepository.refresh()
    }

    private fun ShoppingList.toRecentListUi(now: Instant): RecentListUi {
        val totalItems = items.size
        val acquiredItems = items.count { it.isAcquired }
        val statusType = when {
            totalItems == 0 -> ListStatusType.EMPTY
            acquiredItems == totalItems -> ListStatusType.COMPLETE
            acquiredItems > 0 -> ListStatusType.IN_PROGRESS
            else -> ListStatusType.PENDING
        }
        val statusRes = when (statusType) {
            ListStatusType.EMPTY -> R.string.list_status_empty
            ListStatusType.COMPLETE -> R.string.list_status_complete
            ListStatusType.IN_PROGRESS -> R.string.list_status_in_progress
            ListStatusType.PENDING -> R.string.list_status_pending
        }
        val status = context.getString(statusRes)

        android.util.Log.d("GetHomeListsUseCase", "🔍 toRecentListUi: List '$name' (id=$id)")
        android.util.Log.d("GetHomeListsUseCase", "  - totalItems: $totalItems")
        android.util.Log.d("GetHomeListsUseCase", "  - acquiredItems: $acquiredItems")
        android.util.Log.d("GetHomeListsUseCase", "  - status: $status")
        android.util.Log.d("GetHomeListsUseCase", "  - items.size from domain: ${items.size}")
        android.util.Log.d("GetHomeListsUseCase", "  - items details: ${items.map { "${it.name}(acquired=${it.isAcquired})" }}")

        return RecentListUi(
            id = id,
            name = name,
            date = formatRelativeTime(updatedAt, now),
            itemCount = totalItems,
            completedItemCount = acquiredItems,
            status = status,
            statusType = statusType,
            isShared = sharedWith.isNotEmpty(),
        )
    }

    private fun ShoppingList.toSharedListUi(now: Instant): SharedListUi {
        // For shared lists, we might need to fetch owner details
        // For now, we use ownerId as placeholder (could be enhanced with user service)
        val ownerName = if (ownerId.isNotEmpty()) {
            context.getString(R.string.home_shared_owner_placeholder, ownerId.takeLast(6))
        } else {
            context.getString(R.string.home_shared_owner_unknown)
        }

        return SharedListUi(
            id = id,
            name = name,
            ownerName = ownerName,
            lastUpdated = formatRelativeTime(updatedAt, now),
            avatarUrl = null,
        )
    }

    private fun formatRelativeTime(timestamp: Instant, now: Instant): String {
        val relative = DateUtils.getRelativeTimeSpanString(
            timestamp.toEpochMilli(),
            now.toEpochMilli(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        )
        return context.getString(R.string.common_last_updated, relative)
    }
}
