package com.jrom.mynextfavartist.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val topLevelBackStack: TopLevelBackStack<NavKey> = run {
        val restored = savedStateHandle.get<String>(NAV_STATE_KEY)
            ?.let { raw -> runCatching { json.decodeFromString<SavedNavState>(raw) }.getOrNull() }
        if (restored != null) {
            TopLevelBackStack(
                startKey = Home,
                initialTopLevelKey = restored.topLevelKey,
                initialStacks = restored.stacks,
            )
        } else {
            TopLevelBackStack(startKey = Home)
        }
    }

    val currentTopLevelKey: NavKey
        get() = topLevelBackStack.topLevelKey

    val backStack: SnapshotStateList<NavKey>
        get() = topLevelBackStack.backStack

    fun switchTopLevel(key: NavKey) {
        topLevelBackStack.switchTopLevel(key)
        persist()
    }

    fun addToStack(key: NavKey) {
        topLevelBackStack.add(key)
        persist()
    }

    fun navigateBack() {
        topLevelBackStack.removeLast()
        persist()
    }

    private fun persist() {
        val state = SavedNavState(
            topLevelKey = topLevelBackStack.topLevelKey,
            stacks = topLevelBackStack.snapshotStacks(),
        )
        savedStateHandle[NAV_STATE_KEY] = json.encodeToString(state)
    }

    private companion object {
        const val NAV_STATE_KEY = "nav_state"

        // NavKey is a plain interface from navigation3, not sealed/@Serializable itself, so
        // restoring the back stack across process death needs open polymorphism - every
        // concrete destination has to be registered here. A destination missing from this list
        // fails to decode (caught by runCatching above) and the whole stack falls back to a
        // fresh start instead of crashing, but won't restore.
        val json = Json {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Home::class)
                    subclass(Search::class)
                    subclass(Favorites::class)
                    subclass(ArtistDetails::class)
                }
            }
            allowStructuredMapKeys = true
        }
    }
}

@Serializable
private data class SavedNavState(
    val topLevelKey: NavKey,
    val stacks: Map<NavKey, List<NavKey>>,
)
