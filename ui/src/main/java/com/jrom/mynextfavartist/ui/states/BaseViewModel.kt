package com.jrom.mynextfavartist.ui.states

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Effect>(initialState: State) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    // Channel over SharedFlow: effects (navigation, snackbars) must never be dropped for lack
    // of a collector, unlike state, which is fine to conflate.
    private val _uiEffect = Channel<Effect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    protected fun updateState(reducer: (State) -> State) {
        _uiState.update(reducer)
    }

    protected fun setState(state: State) {
        _uiState.update { state }
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }
}
