package com.jrom.mynextfavartist.di

import javax.inject.Qualifier

/**
 * Distinguishes the IO dispatcher from any other [kotlinx.coroutines.CoroutineDispatcher]
 * binding in the graph. Only Hilt reads this, so it lives beside the module that provides the
 * binding and the modules that request it - all of which are in :app.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IoDispatcher
