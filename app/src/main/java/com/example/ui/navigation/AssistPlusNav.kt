package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppTab
import com.example.model.UserAccount
import com.example.ui.player.Media3Player
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MovieDetailsDialog
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.SeriesDetailsDialog
import com.example.ui.screens.SeriesScreen
import com.example.ui.theme.AssistBgDark
import com.example.ui.theme.AssistCardSurface
import com.example.ui.theme.AssistCyan
import com.example.ui.theme.AssistSurface
import com.example.ui.theme.AssistTextMuted
import com.example.viewmodel.AppViewModel

@Composable
fun AssistPlusNav(
    account: UserAccount,
    viewModel: AppViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val liveCategories by viewModel.liveCategories.collectAsState()
    val selectedLiveCategory by viewModel.selectedLiveCategory.collectAsState()

    val movies by viewModel.movies.collectAsState()
    val movieCategories by viewModel.movieCategories.collectAsState()
    val selectedMovieCategory by viewModel.selectedMovieCategory.collectAsState()

    val series by viewModel.series.collectAsState()
    val seriesCategories by viewModel.seriesCategories.collectAsState()
    val selectedSeriesCategory by viewModel.selectedSeriesCategory.collectAsState()

    val favorites by viewModel.favorites.collectAsState()
    val savedAccounts by viewModel.savedAccounts.collectAsState()
    val currentPlayable by viewModel.currentPlayable.collectAsState()
    val selectedMovie by viewModel.selectedMovie.collectAsState()
    val selectedSeriesDetails by viewModel.selectedSeriesDetails.collectAsState()
    val currentSeriesDetails by viewModel.currentSeriesDetails.collectAsState()
    val isLoadingSeriesDetails by viewModel.isLoadingSeriesDetails.collectAsState()
    val isLoadingContent by viewModel.isLoadingContent.collectAsState()

    // Handle back button
    BackHandler(enabled = currentPlayable != null || currentTab != AppTab.HOME) {
        if (currentPlayable != null) {
            viewModel.closePlayer()
        } else if (currentTab != AppTab.HOME) {
            viewModel.selectTab(AppTab.HOME)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(AssistBgDark)) {
        Scaffold(
            bottomBar = {
                if (currentPlayable == null) {
                    NavigationBar(
                        containerColor = AssistSurface,
                        contentColor = AssistCyan,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppTab.HOME,
                            onClick = { viewModel.selectTab(AppTab.HOME) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Início",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = AssistCyan,
                                indicatorColor = AssistCyan,
                                unselectedIconColor = AssistTextMuted,
                                unselectedTextColor = AssistTextMuted
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.LIVE_TV,
                            onClick = { viewModel.selectTab(AppTab.LIVE_TV) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.LIVE_TV) Icons.Filled.LiveTv else Icons.Outlined.LiveTv,
                                    contentDescription = "Canais",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Canais", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = AssistCyan,
                                indicatorColor = AssistCyan,
                                unselectedIconColor = AssistTextMuted,
                                unselectedTextColor = AssistTextMuted
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.MOVIES,
                            onClick = { viewModel.selectTab(AppTab.MOVIES) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.MOVIES) Icons.Filled.Movie else Icons.Outlined.Movie,
                                    contentDescription = "Filmes",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Filmes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = AssistCyan,
                                indicatorColor = AssistCyan,
                                unselectedIconColor = AssistTextMuted,
                                unselectedTextColor = AssistTextMuted
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.SERIES,
                            onClick = { viewModel.selectTab(AppTab.SERIES) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.SERIES) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                    contentDescription = "Séries",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Séries", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = AssistCyan,
                                indicatorColor = AssistCyan,
                                unselectedIconColor = AssistTextMuted,
                                unselectedTextColor = AssistTextMuted
                            )
                        )

                        NavigationBarItem(
                            selected = currentTab == AppTab.FAVORITES,
                            onClick = { viewModel.selectTab(AppTab.FAVORITES) },
                            icon = {
                                Icon(
                                    if (currentTab == AppTab.FAVORITES) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "Favoritos",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Favoritos", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = AssistCyan,
                                indicatorColor = AssistCyan,
                                unselectedIconColor = AssistTextMuted,
                                unselectedTextColor = AssistTextMuted
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    AppTab.HOME -> {
                        HomeScreen(
                            account = account,
                            savedAccounts = savedAccounts,
                            channels = channels,
                            movies = movies,
                            series = series,
                            favoritesCount = favorites.size,
                            onNavigateTab = { viewModel.selectTab(it) },
                            onPlayChannel = { viewModel.playChannel(it) },
                            onOpenMovie = { viewModel.openMovieDetails(it) },
                            onOpenSeries = { viewModel.openSeriesDetails(it) },
                            onSelectSavedAccount = { viewModel.loginWithSavedAccount(it) },
                            onRenewSubscription = { newExpDate -> viewModel.renewCurrentSubscription(newExpDate) },
                            onLogout = onLogout
                        )
                    }
                    AppTab.LIVE_TV -> {
                        LiveTvScreen(
                            categories = liveCategories,
                            channels = channels,
                            selectedCategory = selectedLiveCategory,
                            favorites = favorites,
                            onSelectCategory = { viewModel.selectLiveCategory(it) },
                            onPlayChannel = { viewModel.playChannel(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            isLoading = isLoadingContent
                        )
                    }
                    AppTab.MOVIES -> {
                        MoviesScreen(
                            categories = movieCategories,
                            movies = movies,
                            selectedCategory = selectedMovieCategory,
                            favorites = favorites,
                            onSelectCategory = { viewModel.selectMovieCategory(it) },
                            onOpenMovie = { viewModel.openMovieDetails(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            isLoading = isLoadingContent
                        )
                    }
                    AppTab.SERIES -> {
                        SeriesScreen(
                            categories = seriesCategories,
                            series = series,
                            selectedCategory = selectedSeriesCategory,
                            favorites = favorites,
                            onSelectCategory = { viewModel.selectSeriesCategory(it) },
                            onOpenSeries = { viewModel.openSeriesDetails(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            isLoading = isLoadingContent
                        )
                    }
                    AppTab.FAVORITES -> {
                        FavoritesScreen(
                            channels = channels,
                            movies = movies,
                            series = series,
                            favorites = favorites,
                            onPlayChannel = { viewModel.playChannel(it) },
                            onOpenMovie = { viewModel.openMovieDetails(it) },
                            onOpenSeries = { viewModel.openSeriesDetails(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            }
        }

        // Movie Details Dialog
        selectedMovie?.let { movie ->
            MovieDetailsDialog(
                movie = movie,
                isFavorite = viewModel.isFavorite(movie.id),
                onToggleFavorite = { viewModel.toggleFavorite(movie.id) },
                onPlay = { viewModel.playMovie(movie) },
                onDismiss = { viewModel.closeMovieDetails() }
            )
        }

        // Series Details Dialog
        if (selectedSeriesDetails != null || isLoadingSeriesDetails) {
            SeriesDetailsDialog(
                details = selectedSeriesDetails,
                isLoading = isLoadingSeriesDetails,
                isFavorite = selectedSeriesDetails?.let { viewModel.isFavorite(it.seriesInfo.id) } ?: false,
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onPlayEpisode = { s, ep -> viewModel.playEpisode(s, ep, selectedSeriesDetails) },
                onDismiss = { viewModel.closeSeriesDetails() }
            )
        }

        // Fullscreen / Overlay Video Player
        AnimatedVisibility(
            visible = currentPlayable != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.fillMaxSize()
        ) {
            currentPlayable?.let { media ->
                Media3Player(
                    media = media,
                    channels = channels,
                    seriesDetails = currentSeriesDetails,
                    onSelectChannel = { ch -> viewModel.playChannel(ch) },
                    onSelectEpisode = { s, ep -> viewModel.playEpisode(s, ep, currentSeriesDetails) },
                    onBack = { viewModel.closePlayer() },
                    onNext = when {
                        media.isLive -> { { viewModel.playNextChannel() } }
                        media.isSeries -> { { viewModel.playNextEpisode() } }
                        else -> null
                    },
                    onPrevious = when {
                        media.isLive -> { { viewModel.playPreviousChannel() } }
                        media.isSeries -> { { viewModel.playPreviousEpisode() } }
                        else -> null
                    }
                )
            }
        }
    }
}
