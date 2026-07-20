package com.jrom.mynextfavartist.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey

class TopLevelBackStack<T : NavKey>(
    private val startKey: T,
    initialTopLevelKey: T = startKey,
    initialStacks: Map<T, List<T>> = mapOf(startKey to listOf(startKey)),
) {

    private var topLevelBackStacks: LinkedHashMap<T, SnapshotStateList<T>> = LinkedHashMap(
        initialStacks.mapValues { (_, stack) -> mutableStateListOf<T>().apply { addAll(stack) } }
    )

    var topLevelKey by mutableStateOf(initialTopLevelKey)
        private set

    val backStack = mutableStateListOf<T>()

    init {
        updateBackStack()
    }

    /** Serializable snapshot of the current stacks, for the caller to persist across process death. */
    fun snapshotStacks(): Map<T, List<T>> = topLevelBackStacks.mapValues { it.value.toList() }

    private fun updateBackStack() {
        backStack.clear()
        val currentStack = topLevelBackStacks[topLevelKey] ?: emptyList()

        if (topLevelKey == startKey) {
            backStack.addAll(currentStack)
        } else {
            val startStack = topLevelBackStacks[startKey] ?: emptyList()
            backStack.addAll(startStack + currentStack)
        }
    }

    fun switchTopLevel(key: T) {
        if (topLevelBackStacks[key] == null) {
            topLevelBackStacks[key] = mutableStateListOf(key)
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T) {
        topLevelBackStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast() {
        val currentStack = topLevelBackStacks[topLevelKey] ?: return

        if (currentStack.size > 1) {
            currentStack.removeLastOrNull()
        } else if (topLevelKey != startKey) {
            topLevelKey = startKey
        }
        updateBackStack()
    }

    fun replaceStack(vararg keys: T) {
        topLevelBackStacks[topLevelKey] = mutableStateListOf(*keys)
        updateBackStack()
    }
}
