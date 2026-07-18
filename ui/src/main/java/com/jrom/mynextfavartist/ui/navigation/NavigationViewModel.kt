package com.jrom.mynextfavartist.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    private val topLevelBackStack = TopLevelBackStack<NavKey>(Home)

    val currentTopLevelKey: NavKey
        get() = topLevelBackStack.topLevelKey

    val backStack: SnapshotStateList<NavKey>
        get() = topLevelBackStack.backStack

    fun switchTopLevel(key: NavKey) {
        topLevelBackStack.switchTopLevel(key)
    }

    fun addToStack(key: NavKey) {
        topLevelBackStack.add(key)
    }

    fun navigateBack() {
        topLevelBackStack.removeLast()
    }

    fun replaceStack(vararg keys: NavKey) {
        topLevelBackStack.replaceStack(*keys)
    }
}
