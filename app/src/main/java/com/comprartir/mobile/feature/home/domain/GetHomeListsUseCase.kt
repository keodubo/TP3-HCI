package com.comprartir.mobile.feature.home.domain

import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.feature.home.model.ListStatusType
import com.comprartir.mobile.feature.home.model.RecentListUi
import com.comprartir.mobile.feature.home.model.SharedListUi
import com.comprartir.mobile.lists.data.ShoppingList
import com.comprartir.mobile.lists.data.ShoppingListsRepository
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
) {

    operator fun invoke(): Flow<HomeListsData> = combine(
        listsRepository.observeLists(),
        authRepository.currentUser,
    ) { allLists, currentUser ->
        val userId = currentUser?.id.orEmpty()
        
        val ownLists = allLists.filter { it.ownerId == userId }
        val sharedLists = allLists.filter { it.ownerId != userId && it.sharedWith.contains(userId) }
        
        val recentListsUi = ownLists
            .sortedByDescending { it.updatedAt }
            .take(5)
            .map { it.toRecentListUi() }

        val sharedListsUi = sharedLists
            .sortedByDescending { it.updatedAt }
            .take(5)
            .map { it.toSharedListUi() }

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

    private fun ShoppingList.toRecentListUi(): RecentListUi {
        val totalItems = items.size
        val acquiredItems = items.count { it.isAcquired }
        val statusType = when {
            totalItems == 0 -> ListStatusType.EMPTY
            acquiredItems == totalItems -> ListStatusType.COMPLETE
            acquiredItems > 0 -> ListStatusType.IN_PROGRESS
            else -> ListStatusType.PENDING
        }
        return RecentListUi(
            id = id,
            name = name,
            updatedAt = updatedAt,
            itemCount = totalItems,
            completedItemCount = acquiredItems,
            statusType = statusType,
            isShared = sharedWith.isNotEmpty(),
        )
    }

    private fun ShoppingList.toSharedListUi(): SharedListUi {
        return SharedListUi(
            id = id,
            name = name,
            ownerId = ownerId,
            updatedAt = updatedAt,
            avatarUrl = null,
        )
    }
}
