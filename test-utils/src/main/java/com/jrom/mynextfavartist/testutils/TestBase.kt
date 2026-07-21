package com.jrom.mynextfavartist.testutils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule

/**
 * Common base for coroutine-heavy unit tests (ViewModels, repositories, data sources) so each
 * test class doesn't have to redeclare the same dispatcher/executor setup.
 *
 * - [instantTaskExecutorRule] makes Architecture Components' background executor (used by
 *   LiveData/Room callbacks) run synchronously instead of on a real background thread.
 * - [mainDispatcherRule] installs [unconfinedTestDispatcher] as `Dispatchers.Main` for the
 *   duration of each test - see [MainDispatcherRule].
 * - [unconfinedTestDispatcher] runs coroutines eagerly (no need to manually advance a virtual
 *   clock), which is what lets [runUnconfinedTest] assert on a StateFlow's value immediately
 *   after triggering an action, without inserting `advanceUntilIdle()` calls everywhere.
 */
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
