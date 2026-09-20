package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppPreferences
import com.example.data.DemoMediaSource
import com.example.data.PresetServers
import com.example.model.AppTab
import com.example.model.CategoryItem
import com.example.model.CategoryType
import com.example.model.ChannelItem
import com.example.model.EpisodeItem
import com.example.model.MovieItem
import com.example.model.PlayableMedia
import com.example.model.SeriesDetails
import com.example.model.SeriesItem
import com.example.model.UserAccount
import com.example.network.M3uParser
import com.example.network.XtreamClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Idle : AuthState()
    data class Loading(
        val message: String = "Conectando aos servidores e usuários...",
        val subMessage: String = "Aguarde enquanto carrega"
    ) : AuthState()
    data class Authenticated(val account: UserAccount) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AppPreferences(application)
    private var loginJob: Job? = null

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Live Channels
    private val _liveCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val liveCategories: StateFlow<List<CategoryItem>> = _liveCategories.asStateFlow()

    private val _channels = MutableStateFlow<List<ChannelItem>>(emptyList())
    val channels: StateFlow<List<ChannelItem>> = _channels.asStateFlow()

    private val _selectedLiveCategory = MutableStateFlow<String>("all")
    val selectedLiveCategory: StateFlow<String> = _selectedLiveCategory.asStateFlow()

    // Movies
    private val _movieCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val movieCategories: StateFlow<List<CategoryItem>> = _movieCategories.asStateFlow()

    private val _movies = MutableStateFlow<List<MovieItem>>(emptyList())
    val movies: StateFlow<List<MovieItem>> = _movies.asStateFlow()

    private val _selectedMovieCategory = MutableStateFlow<String>("all")
    val selectedMovieCategory: StateFlow<String> = _selectedMovieCategory.asStateFlow()

    // Series
    private val _seriesCategories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val seriesCategories: StateFlow<List<CategoryItem>> = _seriesCategories.asStateFlow()

    private val _series = MutableStateFlow<List<SeriesItem>>(emptyList())
    val series: StateFlow<List<SeriesItem>> = _series.asStateFlow()

    private val _selectedSeriesCategory = MutableStateFlow<String>("all")
    val selectedSeriesCategory: StateFlow<String> = _selectedSeriesCategory.asStateFlow()

    // Active playback
    private val _currentPlayable = MutableStateFlow<PlayableMedia?>(null)
    val currentPlayable: StateFlow<PlayableMedia?> = _currentPlayable.asStateFlow()

    // Series Detail Modal State
    private val _selectedSeriesDetails = MutableStateFlow<SeriesDetails?>(null)
    val selectedSeriesDetails: StateFlow<SeriesDetails?> = _selectedSeriesDetails.asStateFlow()

    // Current playing series details (for in-player episode list)
    private val _currentSeriesDetails = MutableStateFlow<SeriesDetails?>(null)
    val currentSeriesDetails: StateFlow<SeriesDetails?> = _currentSeriesDetails.asStateFlow()

    private val _isLoadingSeriesDetails = MutableStateFlow(false)
    val isLoadingSeriesDetails: StateFlow<Boolean> = _isLoadingSeriesDetails.asStateFlow()

    // Movie Detail Modal State
    private val _selectedMovie = MutableStateFlow<MovieItem?>(null)
    val selectedMovie: StateFlow<MovieItem?> = _selectedMovie.asStateFlow()

    // Favorites
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // Saved accounts
    private val _savedAccounts = MutableStateFlow<List<UserAccount>>(emptyList())
    val savedAccounts: StateFlow<List<UserAccount>> = _savedAccounts.asStateFlow()

    // Content loading state
    private val _isLoadingContent = MutableStateFlow(false)
    val isLoadingContent: StateFlow<Boolean> = _isLoadingContent.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        _favorites.value = preferences.getFavorites()
        refreshSavedAccounts()
        checkSavedLogin()
    }

    fun refreshSavedAccounts() {
        _savedAccounts.value = preferences.getSavedAccounts()
    }

    fun removeSavedAccount(username: String) {
        preferences.removeSavedAccount(username)
        refreshSavedAccounts()
    }

    fun loginWithSavedAccount(account: UserAccount) {
        if (account.isM3uMode) {
            loginM3u(account.m3uUrl)
        } else if (account.serverUrl.isNotBlank() && account.username.isNotBlank() && account.password.isNotBlank()) {
            loginJob?.cancel()
            loginJob = viewModelScope.launch {
                _authState.value = AuthState.Loading(
                    message = "Conectando ao usuário ${account.username}...",
                    subMessage = "Aguarde enquanto carrega"
                )
                val result = XtreamClient.authenticate(account.serverUrl, account.username, account.password)
                result.onSuccess { authedAccount ->
                    preferences.saveAccount(authedAccount)
                    refreshSavedAccounts()
                    _authState.value = AuthState.Loading(
                        message = "Carregando canais e conteúdos...",
                        subMessage = "Aguarde enquanto carrega"
                    )
                    loadContentForAccount(authedAccount)
                    _authState.value = AuthState.Authenticated(authedAccount)
                }.onFailure {
                    // Fallback to auto-detect across all candidate servers
                    loginXtreamAuto(account.username, account.password)
                }
            }
        } else if (account.username.isNotBlank() && account.password.isNotBlank()) {
            loginXtreamAuto(account.username, account.password)
        }
    }

    private fun checkSavedLogin() {
        val saved = preferences.getSavedAccount()
        if (saved != null) {
            loginJob?.cancel()
            loginJob = viewModelScope.launch {
                _authState.value = AuthState.Loading(
                    message = "Conectando aos servidores e usuários...",
                    subMessage = "Aguarde enquanto carrega"
                )
                loadContentForAccount(saved)
                _authState.value = AuthState.Authenticated(saved)
            }
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun loginXtreamAuto(user: String, pass: String) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _authState.value = AuthState.Loading(
                message = "Conectando aos servidores e usuários...",
                subMessage = "Aguarde enquanto carrega"
            )
            val result = XtreamClient.autoDetectAndAuthenticate(user.trim(), pass.trim())
            result.onSuccess { account ->
                preferences.saveAccount(account)
                refreshSavedAccounts()
                _authState.value = AuthState.Loading(
                    message = "Carregando catálogo de canais e séries...",
                    subMessage = "Aguarde enquanto carrega"
                )
                loadContentForAccount(account)
                _authState.value = AuthState.Authenticated(account)
            }.onFailure { error ->
                _authState.value = AuthState.Error(
                    error.message ?: "Usuário ou senha incorretos. Verifique os dados digitados."
                )
            }
        }
    }

    fun loginXtream(rawServerUrl: String, user: String, pass: String) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _authState.value = AuthState.Loading(
                message = "Conectando aos servidores e usuários...",
                subMessage = "Aguarde enquanto carrega"
            )
            val cleanUrl = PresetServers.cleanServerUrl(rawServerUrl)
            val result = XtreamClient.authenticate(cleanUrl, user, pass)
            result.onSuccess { account ->
                preferences.saveAccount(account)
                refreshSavedAccounts()
                _authState.value = AuthState.Loading(
                    message = "Carregando catálogo de conteúdos...",
                    subMessage = "Aguarde enquanto carrega"
                )
                loadContentForAccount(account)
                _authState.value = AuthState.Authenticated(account)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Falha ao conectar no servidor")
            }
        }
    }

    fun loginM3u(rawM3uUrl: String) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _authState.value = AuthState.Loading(
                message = "Processando lista M3U...",
                subMessage = "Aguarde enquanto carrega"
            )
            val cleanUrl = PresetServers.cleanM3uUrl(rawM3uUrl)
            val result = M3uParser.loadFromUrl(cleanUrl)
            result.onSuccess { parsed ->
                val account = UserAccount(
                    username = "M3U Playlist",
                    serverUrl = cleanUrl,
                    serverName = "Lista M3U",
                    status = "Ativo",
                    isM3uMode = true,
                    m3uUrl = cleanUrl
                )
                preferences.saveAccount(account)
                refreshSavedAccounts()
                _channels.value = parsed.liveChannels
                _movies.value = parsed.movies
                _series.value = parsed.series
                _liveCategories.value = parsed.liveCategories
                _movieCategories.value = parsed.movieCategories
                _seriesCategories.value = parsed.seriesCategories
                _authState.value = AuthState.Authenticated(account)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Falha ao carregar link M3U")
            }
        }
    }

    fun cancelLoading() {
        loginJob?.cancel()
        loginJob = null
        val saved = preferences.getSavedAccount()
        if (saved != null) {
            _authState.value = AuthState.Authenticated(saved)
        } else {
            _authState.value = AuthState.Idle
        }
    }

    /**
     * Activates or creates a VIP account once payment is confirmed via Mercado Pago
     */
    fun activatePaidSubscription(username: String, durationDays: Int, expDate: String) {
        loginJob?.cancel()
        loginJob = viewModelScope.launch {
            _authState.value = AuthState.Loading(
                message = "Ativando assinatura Mercado Pago...",
                subMessage = "Aguarde enquanto carrega seu acesso VIP"
            )
            val currentAccount = (_authState.value as? AuthState.Authenticated)?.account
            val targetUser = if (username.isNotBlank()) username else "VIP_${(1000..9999).random()}"
            val updatedAccount = currentAccount?.copy(
                expDate = expDate,
                status = "Ativo (Mercado Pago)"
            ) ?: UserAccount(
                username = targetUser,
                password = "vip",
                serverUrl = PresetServers.candidateServerUrls.first(),
                serverName = "Assist+ VIP Mercado Pago",
                status = "Ativo (Mercado Pago)",
                expDate = expDate
            )

            preferences.saveAccount(updatedAccount)
            refreshSavedAccounts()
            loadContentForAccount(updatedAccount)
            _authState.value = AuthState.Authenticated(updatedAccount)
        }
    }

    /**
     * Renews the currently authenticated user's subscription
     */
    fun renewCurrentSubscription(newExpDate: String) {
        val current = (_authState.value as? AuthState.Authenticated)?.account ?: return
        val updated = current.copy(
            expDate = newExpDate,
            status = "Ativo (Mercado Pago)"
        )
        preferences.saveAccount(updated)
        refreshSavedAccounts()
        _authState.value = AuthState.Authenticated(updated)
    }

    fun logout() {
        loginJob?.cancel()
        loginJob = null
        preferences.clearAccount()
        refreshSavedAccounts()
        _authState.value = AuthState.Idle
        _channels.value = emptyList()
        _movies.value = emptyList()
        _series.value = emptyList()
        _currentPlayable.value = null
    }

    private suspend fun loadContentForAccount(account: UserAccount) {
        if (account.isM3uMode) {
            val cleanUrl = PresetServers.cleanM3uUrl(account.m3uUrl)
            val result = M3uParser.loadFromUrl(cleanUrl)
            result.onSuccess { parsed ->
                _channels.value = parsed.liveChannels
                _movies.value = parsed.movies
                _series.value = parsed.series
                _liveCategories.value = parsed.liveCategories
                _movieCategories.value = parsed.movieCategories
                _seriesCategories.value = parsed.seriesCategories
            }
            return
        }

        _isLoadingContent.value = true
        try {
            // Fetch categories
            val liveCats = XtreamClient.getLiveCategories(account.serverUrl, account.username, account.password)
            val movieCats = XtreamClient.getVodCategories(account.serverUrl, account.username, account.password)
            val seriesCats = XtreamClient.getSeriesCategories(account.serverUrl, account.username, account.password)

            _liveCategories.value = if (liveCats.isNotEmpty()) liveCats else DemoMediaSource.demoLiveCategories
            _movieCategories.value = if (movieCats.isNotEmpty()) movieCats else DemoMediaSource.demoMovieCategories
            _seriesCategories.value = if (seriesCats.isNotEmpty()) seriesCats else DemoMediaSource.demoSeriesCategories

            // Fetch initial channels, movies, series
            val streams = XtreamClient.getLiveStreams(account.serverUrl, account.username, account.password)
            val vods = XtreamClient.getVodStreams(account.serverUrl, account.username, account.password)
            val seriesList = XtreamClient.getSeries(account.serverUrl, account.username, account.password)

            _channels.value = if (streams.isNotEmpty()) streams else DemoMediaSource.demoChannels
            _movies.value = if (vods.isNotEmpty()) vods else DemoMediaSource.demoMovies
            _series.value = if (seriesList.isNotEmpty()) seriesList else DemoMediaSource.demoSeries
        } catch (e: Exception) {
            // Fallback to demo items if server request has issue
            _channels.value = DemoMediaSource.demoChannels
            _movies.value = DemoMediaSource.demoMovies
            _series.value = DemoMediaSource.demoSeries
        } finally {
            _isLoadingContent.value = false
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLiveCategory(catId: String) {
        _selectedLiveCategory.value = catId
        val auth = _authState.value
        if (auth is AuthState.Authenticated && !auth.account.isM3uMode && auth.account.username != "Demonstração") {
            viewModelScope.launch {
                val streams = XtreamClient.getLiveStreams(
                    auth.account.serverUrl,
                    auth.account.username,
                    auth.account.password,
                    categoryId = catId
                )
                if (streams.isNotEmpty()) {
                    _channels.value = streams
                }
            }
        }
    }

    fun selectMovieCategory(catId: String) {
        _selectedMovieCategory.value = catId
        val auth = _authState.value
        if (auth is AuthState.Authenticated && !auth.account.isM3uMode && auth.account.username != "Demonstração") {
            viewModelScope.launch {
                val vods = XtreamClient.getVodStreams(
                    auth.account.serverUrl,
                    auth.account.username,
                    auth.account.password,
                    categoryId = catId
                )
                if (vods.isNotEmpty()) {
                    _movies.value = vods
                }
            }
        }
    }

    fun selectSeriesCategory(catId: String) {
        _selectedSeriesCategory.value = catId
        val auth = _authState.value
        if (auth is AuthState.Authenticated && !auth.account.isM3uMode && auth.account.username != "Demonstração") {
            viewModelScope.launch {
                val seriesList = XtreamClient.getSeries(
                    auth.account.serverUrl,
                    auth.account.username,
                    auth.account.password,
                    categoryId = catId
                )
                if (seriesList.isNotEmpty()) {
                    _series.value = seriesList
                }
            }
        }
    }

    fun playChannel(channel: ChannelItem) {
        _currentPlayable.value = PlayableMedia(
            id = channel.id,
            title = channel.name,
            subtitle = channel.categoryName,
            streamUrl = channel.streamUrl,
            isLive = true,
            logoUrl = channel.streamIcon,
            category = channel.categoryName
        )
    }

    fun playMovie(movie: MovieItem) {
        _selectedMovie.value = null
        _currentPlayable.value = PlayableMedia(
            id = movie.id,
            title = movie.name,
            subtitle = "${movie.releaseDate ?: ""} • ${movie.duration ?: ""}".trim(' ', '•'),
            streamUrl = movie.streamUrl,
            isLive = false,
            logoUrl = movie.streamIcon,
            category = movie.categoryName
        )
    }

    fun playEpisode(series: SeriesItem, episode: EpisodeItem, details: SeriesDetails? = null) {
        val seriesInfo = details ?: _selectedSeriesDetails.value
        if (seriesInfo != null) {
            _currentSeriesDetails.value = seriesInfo
        }
        _selectedSeriesDetails.value = null
        _currentPlayable.value = PlayableMedia(
            id = episode.id,
            title = series.name,
            subtitle = "T${episode.seasonNum}:E${episode.episodeNum} • ${episode.title}",
            streamUrl = episode.streamUrl,
            isLive = false,
            isSeries = true,
            seriesId = series.id,
            seasonNum = episode.seasonNum,
            episodeNum = episode.episodeNum,
            logoUrl = episode.cover ?: series.cover,
            category = series.categoryName
        )
    }

    fun openMovieDetails(movie: MovieItem) {
        _selectedMovie.value = movie
    }

    fun closeMovieDetails() {
        _selectedMovie.value = null
    }

    fun openSeriesDetails(series: SeriesItem) {
        val auth = _authState.value
        if (auth is AuthState.Authenticated && !auth.account.isM3uMode && auth.account.username != "Demonstração") {
            viewModelScope.launch {
                _isLoadingSeriesDetails.value = true
                val details = XtreamClient.getSeriesDetails(
                    auth.account.serverUrl,
                    auth.account.username,
                    auth.account.password,
                    series
                )
                _selectedSeriesDetails.value = details
                _isLoadingSeriesDetails.value = false
            }
        } else {
            _selectedSeriesDetails.value = DemoMediaSource.getDemoSeriesDetails(series.id)
        }
    }

    fun closeSeriesDetails() {
        _selectedSeriesDetails.value = null
    }

    fun closePlayer() {
        _currentPlayable.value = null
    }

    fun toggleFavorite(id: String) {
        preferences.toggleFavorite(id)
        _favorites.value = preferences.getFavorites()
    }

    fun isFavorite(id: String): Boolean {
        return _favorites.value.contains(id)
    }

    fun playNextChannel() {
        val current = _currentPlayable.value ?: return
        val list = _channels.value
        val idx = list.indexOfFirst { it.id == current.id }
        if (idx != -1 && idx < list.size - 1) {
            playChannel(list[idx + 1])
        }
    }

    fun playPreviousChannel() {
        val current = _currentPlayable.value ?: return
        val list = _channels.value
        val idx = list.indexOfFirst { it.id == current.id }
        if (idx > 0) {
            playChannel(list[idx - 1])
        }
    }

    fun playNextEpisode() {
        val media = _currentPlayable.value ?: return
        if (!media.isSeries) return
        val details = _currentSeriesDetails.value ?: return
        val allEpisodes = details.episodesBySeason.values.flatten()
        val currentIdx = allEpisodes.indexOfFirst { it.id == media.id || (it.seasonNum == media.seasonNum && it.episodeNum == media.episodeNum) }
        if (currentIdx != -1 && currentIdx < allEpisodes.size - 1) {
            val nextEp = allEpisodes[currentIdx + 1]
            playEpisode(details.seriesInfo, nextEp, details)
        }
    }

    fun playPreviousEpisode() {
        val media = _currentPlayable.value ?: return
        if (!media.isSeries) return
        val details = _currentSeriesDetails.value ?: return
        val allEpisodes = details.episodesBySeason.values.flatten()
        val currentIdx = allEpisodes.indexOfFirst { it.id == media.id || (it.seasonNum == media.seasonNum && it.episodeNum == media.episodeNum) }
        if (currentIdx > 0) {
            val prevEp = allEpisodes[currentIdx - 1]
            playEpisode(details.seriesInfo, prevEp, details)
        }
    }
}
