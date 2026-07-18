package com.jrom.mynextfavartist.ui.states

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrom.mynextfavartist.ui.utils.singleSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<State, Effect>(initialState: State) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _uiEffect = singleSharedFlow<Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    protected fun updateState(reducer: (State) -> State) {
        _uiState.update(reducer)
    }

    protected fun setState(state: State) {
        _uiState.update { state }
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }
}
