package com.jrom.mynextfavartist.testutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher

/**
 * Code under test (ViewModels, repositories) launches coroutines on `Dispatchers.Main`, which
 * has no real implementation on the JVM and throws if used unmodified in a unit test. This
 * rule swaps it for [testDispatcher] before each test and restores it after, so `viewModelScope`
 * and similar `Dispatchers.Main`-based launches work without an Android instrumentation test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val testDispatcher: TestDispatcher) : TestWatcher() {

    override fun starting(description: org.junit.runner.Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: org.junit.runner.Description?) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}
