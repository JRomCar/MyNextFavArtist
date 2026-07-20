package com.jrom.mynextfavartist.ui.utils

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

@SuppressLint("ComposableNaming")
@Composable
fun <T> Flow<T>.collectWithEffect(effect: suspend (T) -> Unit) {
    val currentEffect by rememberUpdatedState(effect)
    val lifecycleOwner = LocalLifecycleOwner.current
    val flow = this
    LaunchedEffect(flow, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { currentEffect(it) }
        }
    }
}
