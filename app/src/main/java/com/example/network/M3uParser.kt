package com.example.network

import com.example.data.PresetServers
import com.example.model.CategoryItem
import com.example.model.CategoryType
import com.example.model.ChannelItem
import com.example.model.MovieItem
import com.example.model.SeriesItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ParsedM3uContent(
    val liveChannels: List<ChannelItem>,
    val movies: List<MovieItem>,
    val series: List<SeriesItem>,
    val liveCategories: List<CategoryItem>,
    val movieCategories: List<CategoryItem>,
    val seriesCategories: List<CategoryItem>
)

object M3uParser {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val TVG_NAME_PATTERN = Pattern.compile("tvg-name=\"([^\"]*)\"")
    private val TVG_LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]*)\"")
    private val TVG_ID_PATTERN = Pattern.compile("tvg-id=\"([^\"]*)\"")
    private val GROUP_TITLE_PATTERN = Pattern.compile("group-title=\"([^\"]*)\"")

    suspend fun loadFromUrl(url: String): Result<ParsedM3uContent> = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = PresetServers.cleanM3uUrl(url)
            val request = Request.Builder()
                .url(cleanUrl)
                .header("User-Agent", "IPTVSmartersPro/AssistPlus (Android; Mobile)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Erro ao carregar M3U: HTTP ${response.code}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("M3U vazio"))
            val parsed = parseContent(body)
            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseContent(content: String): ParsedM3uContent {
        val reader = BufferedReader(StringReader(content))
        var line: String? = reader.readLine()

        val channels = mutableListOf<ChannelItem>()
        val movies = mutableListOf<MovieItem>()
        val series = mutableListOf<SeriesItem>()

        val liveCatMap = mutableMapOf<String, String>()
        val movieCatMap = mutableMapOf<String, String>()
        val seriesCatMap = mutableMapOf<String, String>()

        var currentExtInf: String? = null
        var index = 0

        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                currentExtInf = trimmed
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("#") && currentExtInf != null) {
                val streamUrl = trimmed
                val meta = parseExtInf(currentExtInf, streamUrl, index++)

                val lowerGroup = meta.group.lowercase()
                val lowerUrl = streamUrl.lowercase()

                val isMovie = lowerGroup.contains("filme") || lowerGroup.contains("movie") || lowerGroup.contains("vod") ||
                        (lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mkv")) && !lowerGroup.contains("ao vivo")
                val isSeries = lowerGroup.contains("serie") || lowerGroup.contains("série") ||
                        lowerGroup.contains("temporada") || meta.name.matches(Regex(".*[sS]\\d{1,2}[eE]\\d{1,2}.*"))

                when {
                    isSeries -> {
                        seriesCatMap[meta.group] = meta.group
                        series.add(
                            SeriesItem(
                                id = meta.id,
                                name = meta.name,
                                cover = meta.logo,
                                categoryId = meta.group,
                                categoryName = meta.group
                            )
                        )
                    }
                    isMovie -> {
                        movieCatMap[meta.group] = meta.group
                        movies.add(
                            MovieItem(
                                id = meta.id,
                                name = meta.name,
                                streamIcon = meta.logo,
                                streamUrl = streamUrl,
                                categoryId = meta.group,
                                categoryName = meta.group
                            )
                        )
                    }
                    else -> {
                        liveCatMap[meta.group] = meta.group
                        channels.add(
                            ChannelItem(
                                id = meta.id,
                                name = meta.name,
                                streamIcon = meta.logo,
                                streamUrl = streamUrl,
                                categoryId = meta.group,
                                categoryName = meta.group,
                                num = channels.size + 1
                            )
                        )
                    }
                }
                currentExtInf = null
            }
            line = reader.readLine()
        }

        val liveCategories = mutableListOf(CategoryItem("all", "Todos os Canais", CategoryType.LIVE))
        liveCatMap.keys.sorted().forEach { cat ->
            liveCategories.add(CategoryItem(cat, cat, CategoryType.LIVE))
        }

        val movieCategories = mutableListOf(CategoryItem("all", "Todos os Filmes", CategoryType.MOVIE))
        movieCatMap.keys.sorted().forEach { cat ->
            movieCategories.add(CategoryItem(cat, cat, CategoryType.MOVIE))
        }

        val seriesCategories = mutableListOf(CategoryItem("all", "Todas as Séries", CategoryType.SERIES))
        seriesCatMap.keys.sorted().forEach { cat ->
            seriesCategories.add(CategoryItem(cat, cat, CategoryType.SERIES))
        }

        return ParsedM3uContent(
            liveChannels = channels,
            movies = movies,
            series = series,
            liveCategories = liveCategories,
            movieCategories = movieCategories,
            seriesCategories = seriesCategories
        )
    }

    private data class ParsedMeta(
        val id: String,
        val name: String,
        val logo: String?,
        val group: String
    )

    private fun parseExtInf(extinf: String, streamUrl: String, index: Int): ParsedMeta {
        var logo: String? = null
        var group = "Geral"
        var name = "Canal $index"
        var id = "ch_$index"

        val logoMatcher = TVG_LOGO_PATTERN.matcher(extinf)
        if (logoMatcher.find()) {
            logo = logoMatcher.group(1)?.takeIf { it.isNotBlank() }
        }

        val idMatcher = TVG_ID_PATTERN.matcher(extinf)
        if (idMatcher.find()) {
            val parsedId = idMatcher.group(1)?.trim()
            if (!parsedId.isNullOrBlank()) id = parsedId
        }

        val groupMatcher = GROUP_TITLE_PATTERN.matcher(extinf)
        if (groupMatcher.find()) {
            val parsedGroup = groupMatcher.group(1)?.trim()
            if (!parsedGroup.isNullOrBlank()) group = parsedGroup
        }

        val commaIdx = extinf.lastIndexOf(',')
        if (commaIdx != -1 && commaIdx < extinf.length - 1) {
            name = extinf.substring(commaIdx + 1).trim()
        } else {
            val nameMatcher = TVG_NAME_PATTERN.matcher(extinf)
            if (nameMatcher.find()) {
                val parsedName = nameMatcher.group(1)?.trim()
                if (!parsedName.isNullOrBlank()) name = parsedName
            }
        }

        return ParsedMeta(id = id, name = name, logo = logo, group = group)
    }
}
