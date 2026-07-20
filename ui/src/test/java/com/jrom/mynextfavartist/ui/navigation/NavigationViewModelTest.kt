package com.jrom.mynextfavartist.ui.navigation

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationViewModelTest {

    @Test
    fun `fresh start has Home as the only backstack entry`() {
        val sut = NavigationViewModel(SavedStateHandle())

        assertEquals(Home, sut.currentTopLevelKey)
        assertEquals(listOf(Home), sut.backStack.toList())
    }

    @Test
    fun `navigation state survives a process death via SavedStateHandle`() {
        val artist = ArtistNavArg(
            mbid = "a74b1b7f-71a5-4011-9441-d0b5e4122711",
            name = "Radiohead",
            type = "Group",
            country = "GB",
            disambiguation = null,
        )
        val savedStateHandle = SavedStateHandle()
        val original = NavigationViewModel(savedStateHandle)

        original.switchTopLevel(Search)
        original.addToStack(ArtistDetails(artist))

        // A new ViewModel instance backed by the same SavedStateHandle simulates the process
        // being killed and the activity/ViewModel recreated from the saved instance state.
        val restored = NavigationViewModel(savedStateHandle)

        assertEquals(ArtistDetails(artist), restored.backStack.last())
        assertEquals(original.backStack.toList(), restored.backStack.toList())
        assertEquals(original.currentTopLevelKey, restored.currentTopLevelKey)
    }

    @Test
    fun `navigating back after restore still falls back to the start destination`() {
        val savedStateHandle = SavedStateHandle()
        val original = NavigationViewModel(savedStateHandle)
        original.switchTopLevel(Favorites)

        val restored = NavigationViewModel(savedStateHandle)
        restored.navigateBack()

        assertEquals(Home, restored.currentTopLevelKey)
        assertEquals(listOf(Home), restored.backStack.toList())
    }

    @Test
    fun `malformed saved state falls back to a fresh start instead of crashing`() {
        val savedStateHandle = SavedStateHandle(mapOf("nav_state" to "not valid json"))

        val sut = NavigationViewModel(savedStateHandle)

        assertEquals(Home, sut.currentTopLevelKey)
        assertEquals(listOf(Home), sut.backStack.toList())
    }
}
