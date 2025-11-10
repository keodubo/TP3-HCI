package com.comprartir.mobile.feature.listdetail.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.comprartir.mobile.feature.listdetail.data.FakeListDetailRepository
import com.comprartir.mobile.feature.listdetail.model.ListDetailEffect
import com.comprartir.mobile.feature.listdetail.model.ListDetailEvent
import com.comprartir.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private fun createViewModel(): ListDetailViewModel {
        val repository = FakeListDetailRepository()
        val savedStateHandle = SavedStateHandle(mapOf("listId" to "demo"))
        return ListDetailViewModel(repository, savedStateHandle)
    }

    @Test
    fun `initial state loads items`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.items.isNotEmpty())
    }

    @Test
    fun `deleting an item emits undo effect`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val effectDeferred = async { viewModel.effects.first { it is ListDetailEffect.ShowUndoDelete } }

        val firstItem = viewModel.state.value.items.first()
        viewModel.onEvent(ListDetailEvent.DeleteItem(firstItem.id))
        advanceUntilIdle()

        val effect = effectDeferred.await()
        assertTrue(effect is ListDetailEffect.ShowUndoDelete)
    }

    @Test
    fun `hide completed filter removes completed items`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(ListDetailEvent.ToggleHideCompleted)
        val filtered = viewModel.state.value.visibleItems

        assertTrue(filtered.none { it.isCompleted })
    }
}

