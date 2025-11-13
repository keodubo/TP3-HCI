package com.comprartir.mobile.feature.home.domain

import com.comprartir.mobile.auth.data.AuthRepository
import com.comprartir.mobile.feature.home.model.RecentListUi
import com.comprartir.mobile.feature.home.model.SharedListUi
import com.comprartir.mobile.lists.data.ShoppingList
import com.comprartir.mobile.lists.data.ShoppingListsRepository
import java.time.Duration
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
            .map { it.toSharedListUi(now, userId) }

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
        val status = when {
            totalItems == 0 -> "Vacía"
            acquiredItems == totalItems -> "Completada"
            acquiredItems > 0 -> "En progreso"
            else -> "Pendiente"
        }

        return RecentListUi(
            id = id,
            name = name,
            date = formatRelativeTime(updatedAt, now),
            itemCount = totalItems,
            status = status,
            isShared = sharedWith.isNotEmpty(),
        )
    }

    private fun ShoppingList.toSharedListUi(now: Instant, currentUserId: String): SharedListUi {
        // For shared lists, we might need to fetch owner details
        // For now, we use ownerId as placeholder (could be enhanced with user service)
        val ownerName = if (ownerId.isNotEmpty()) {
            "Usuario $ownerId" // Placeholder - ideally fetch from user service
        } else {
            "Desconocido"
        }

        return SharedListUi(
            id = id,
            name = name,
            ownerName = ownerName,
            lastUpdated = "Actualizada ${formatRelativeTime(updatedAt, now).lowercase()}",
            avatarUrl = null,
        )
    }

    private fun formatRelativeTime(timestamp: Instant, now: Instant): String {
        val duration = Duration.between(timestamp, now)
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()

        return when {
            minutes < 1 -> "Ahora"
            minutes < 60 -> "Hace ${minutes}m"
            hours == 1L -> "Hace 1 hora"
            hours < 24 -> "Hace ${hours} horas"
            days == 1L -> "Ayer"
            days < 7 -> "Hace ${days} días"
            days < 14 -> "Hace 1 semana"
            days < 30 -> "Hace ${days / 7} semanas"
            days < 60 -> "Hace 1 mes"
            else -> "Hace ${days / 30} meses"
        }
    }
}
