package com.jrom.mynextfavartist.testutils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule

open class TestBase {
    @OptIn(ExperimentalCoroutinesApi::class)
    val unconfinedTestDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule(
        testDispatcher = unconfinedTestDispatcher
    )

    fun runUnconfinedTest(block: suspend TestScope.() -> Unit) = runTest(
        context = unconfinedTestDispatcher,
        testBody = block
    )
}
