package com.comprartir.mobile.feature.lists.viewmodel

import com.comprartir.mobile.feature.lists.data.FakeListsRepository
import com.comprartir.mobile.feature.lists.model.ListsEffect
import com.comprartir.mobile.feature.lists.model.ListsEvent
import com.comprartir.mobile.feature.lists.model.SortOption
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
class ListsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository = FakeListsRepository()

    @Test
    fun `initial state loads sample lists`() = runTest {
        val viewModel = ListsViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(5, state.lists.size)
    }

    @Test
    fun `search query updates filtered lists`() = runTest {
        val viewModel = ListsViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(ListsEvent.SearchQueryChanged("fiesta"))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1, state.lists.size)
        assertEquals("Fiesta sábado", state.lists.first().name)
    }

    @Test
    fun `changing sort option triggers reorder`() = runTest {
        val viewModel = ListsViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(ListsEvent.SortOptionSelected(SortOption.NAME))
        advanceUntilIdle()

        val reorderedFirst = viewModel.state.value.lists.first().name
        assertEquals("Compra semanal", reorderedFirst)
    }

    @Test
    fun `create list dialog becomes visible after event`() = runTest {
        val viewModel = ListsViewModel(repository)
        advanceUntilIdle()

        viewModel.onEvent(ListsEvent.CreateList)

        assertTrue(viewModel.state.value.createListState.isVisible)
    }

    @Test
    fun `confirm create list emits navigation effect`() = runTest {
        val viewModel = ListsViewModel(repository)
        advanceUntilIdle()
        val effectDeferred = async { viewModel.effects.first() }

        viewModel.onEvent(ListsEvent.CreateList)
        viewModel.onEvent(ListsEvent.CreateListNameChanged("Lista nueva"))
        viewModel.onEvent(ListsEvent.ConfirmCreateList)
        advanceUntilIdle()

        val effect = effectDeferred.await()
        assertTrue(effect is ListsEffect.NavigateToListDetail)
        assertFalse(viewModel.state.value.createListState.isVisible)
    }
}
