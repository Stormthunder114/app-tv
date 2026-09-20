package com.example.model

data class ServerConfig(
    val name: String,
    val url: String,
    val description: String = ""
)

data class UserAccount(
    val username: String = "",
    val password: String = "",
    val serverUrl: String = "",
    val serverName: String = "",
    val expDate: String = "",
    val status: String = "Active",
    val maxConnections: String = "1",
    val activeConnections: String = "0",
    val isTrial: Boolean = false,
    val isM3uMode: Boolean = false,
    val m3uUrl: String = ""
)

data class CategoryItem(
    val id: String,
    val name: String,
    val type: CategoryType = CategoryType.LIVE
)

enum class CategoryType {
    LIVE, MOVIE, SERIES
}

data class ChannelItem(
    val id: String,
    val name: String,
    val streamIcon: String? = null,
    val streamUrl: String,
    val categoryId: String = "",
    val categoryName: String = "Geral",
    val num: Int = 0,
    val epgChannelId: String? = null,
    val isFavorite: Boolean = false
)

data class MovieItem(
    val id: String,
    val name: String,
    val streamIcon: String? = null,
    val streamUrl: String,
    val categoryId: String = "",
    val categoryName: String = "Filmes",
    val rating: String? = null,
    val releaseDate: String? = null,
    val plot: String? = null,
    val duration: String? = null,
    val containerExtension: String = "mp4",
    val isFavorite: Boolean = false
)

data class SeriesItem(
    val id: String,
    val name: String,
    val cover: String? = null,
    val categoryId: String = "",
    val categoryName: String = "Séries",
    val rating: String? = null,
    val releaseDate: String? = null,
    val plot: String? = null,
    val isFavorite: Boolean = false
)

data class SeasonItem(
    val seasonNumber: Int,
    val name: String,
    val episodeCount: Int = 0
)

data class EpisodeItem(
    val id: String,
    val episodeNum: Int,
    val seasonNum: Int,
    val title: String,
    val containerExtension: String = "mp4",
    val streamUrl: String,
    val duration: String? = null,
    val plot: String? = null,
    val cover: String? = null
)

data class SeriesDetails(
    val seriesInfo: SeriesItem,
    val seasons: List<SeasonItem> = emptyList(),
    val episodesBySeason: Map<Int, List<EpisodeItem>> = emptyMap()
)

data class PlayableMedia(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val streamUrl: String,
    val isLive: Boolean = false,
    val logoUrl: String? = null,
    val category: String? = null,
    val isSeries: Boolean = false,
    val seriesId: String? = null,
    val seasonNum: Int = 1,
    val episodeNum: Int = 1
)

enum class AppTab {
    HOME, LIVE_TV, MOVIES, SERIES, FAVORITES
}
