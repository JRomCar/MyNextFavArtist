package com.jrom.mynextfavartist.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.jrom.mynextfavartist.ui.details.DetailsScreen
import com.jrom.mynextfavartist.ui.favorites.FavoritesScreen
import com.jrom.mynextfavartist.ui.home.HomeScreen
import com.jrom.mynextfavartist.ui.navigation.ArtistDetails
import com.jrom.mynextfavartist.ui.navigation.Favorites
import com.jrom.mynextfavartist.ui.navigation.Home
import com.jrom.mynextfavartist.ui.navigation.NavigationViewModel
import com.jrom.mynextfavartist.ui.navigation.Search
import com.jrom.mynextfavartist.ui.navigation.toDomain
import com.jrom.mynextfavartist.ui.navigation.toNavArg
import com.jrom.mynextfavartist.ui.search.SearchScreen

@Composable
fun MyNextFavArtistApp() {
    val bottomNavItems = listOf(Home, Search, Favorites)
    val navigationViewModel = hiltViewModel<NavigationViewModel>()
    val baseModifier = Modifier.fillMaxSize()
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = navigationViewModel.currentTopLevelKey == item
                    val title = stringResource(item.titleRes)
                    NavigationBarItem(
                        selected = selected,
                        onClick = dropUnlessResumed {
                            navigationViewModel.switchTopLevel(item)
                        },
                        icon = {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = title
                            )
                        },
                        label = {
                            Text(title)
                        },
                    )
                }
            }
        },
        modifier = baseModifier
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            backStack = navigationViewModel.backStack,
            onBack = { navigationViewModel.navigateBack() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<Home> {
                    HomeScreen(
                        modifier = baseModifier,
                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                        onDetailClick = { artist ->
                            navigationViewModel.addToStack(ArtistDetails(artist.toNavArg()))
                        },
                    )
                }
                entry<Favorites> {
                    FavoritesScreen(
                        modifier = baseModifier,
                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                        onDetailClick = { artist ->
                            navigationViewModel.addToStack(ArtistDetails(artist.toNavArg()))
                        },
                    )
                }
                entry<Search> {
                    SearchScreen(
                        modifier = baseModifier,
                        contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                        onDetailClick = { artist ->
                            navigationViewModel.addToStack(ArtistDetails(artist.toNavArg()))
                        },
                    )
                }
                entry<ArtistDetails> { args ->
                    DetailsScreen(
                        modifier = baseModifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        artist = args.artist.toDomain(),
                        onBackClick = dropUnlessResumed { navigationViewModel.navigateBack() },
                    )
                }
            }
        )
    }
}
