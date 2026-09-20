package com.example.network

import com.example.data.PresetServers
import com.example.model.CategoryItem
import com.example.model.CategoryType
import com.example.model.ChannelItem
import com.example.model.EpisodeItem
import com.example.model.MovieItem
import com.example.model.SeasonItem
import com.example.model.SeriesDetails
import com.example.model.SeriesItem
import com.example.model.UserAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object XtreamClient {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private const val USER_AGENT = "IPTVSmartersPro/AssistPlus (Android; Mobile)"

    private fun isAccountAuthenticated(userInfo: JSONObject?): Boolean {
        if (userInfo == null) return false
        val authInt = userInfo.optInt("auth", -1)
        val authStr = userInfo.optString("auth", "")
        val authBool = userInfo.optBoolean("auth", false)
        val isAuth = (authInt == 1) || (authStr == "1") || authBool
        if (!isAuth) return false

        val status = userInfo.optString("status", "")
        if (status.equals("Disabled", ignoreCase = true) ||
            status.equals("Banned", ignoreCase = true) ||
            status.equals("Expired", ignoreCase = true)) {
            return false
        }
        return true
    }

    suspend fun autoDetectAndAuthenticate(
        user: String,
        pass: String,
        candidateServers: List<String> = PresetServers.candidateServerUrls
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        if (candidateServers.isEmpty()) {
            return@withContext Result.failure(Exception("Nenhum servidor cadastrado para teste automático"))
        }

        val trimmedUser = user.trim()
        val trimmedPass = pass.trim()
        val encodedUser = try { URLEncoder.encode(trimmedUser, "UTF-8") } catch (_: Exception) { trimmedUser }
        val encodedPass = try { URLEncoder.encode(trimmedPass, "UTF-8") } catch (_: Exception) { trimmedPass }

        val testClient = client.newBuilder()
            .connectTimeout(7, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val channel = Channel<UserAccount?>(candidateServers.size)
        val jobs = candidateServers.map { serverUrl ->
            launch {
                val cleanUrl = PresetServers.cleanServerUrl(serverUrl)
                try {
                    val url = "$cleanUrl/player_api.php?username=$encodedUser&password=$encodedPass"
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", USER_AGENT)
                        .build()

                    val response = testClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string()
                        if (bodyString != null && bodyString.trimStart().startsWith("{")) {
                            val json = JSONObject(bodyString)
                            val userInfo = json.optJSONObject("user_info")
                            if (isAccountAuthenticated(userInfo)) {
                                val expTimestampStr = userInfo?.optString("exp_date", "") ?: ""
                                val expDateFormatted = formatTimestamp(expTimestampStr)
                                val detectedName = PresetServers.getServerDisplayName(cleanUrl)
                                val account = UserAccount(
                                    username = userInfo?.optString("username", trimmedUser) ?: trimmedUser,
                                    password = trimmedPass,
                                    serverUrl = cleanUrl,
                                    serverName = detectedName,
                                    expDate = expDateFormatted,
                                    status = userInfo?.optString("status", "Ativo") ?: "Ativo",
                                    maxConnections = userInfo?.optString("max_connections", "1") ?: "1",
                                    activeConnections = userInfo?.optString("active_cons", "0") ?: "0",
                                    isTrial = userInfo?.optString("is_trial", "0") == "1",
                                    isM3uMode = false
                                )
                                channel.send(account)
                                return@launch
                            }
                        }
                    }
                } catch (_: Exception) {
                }
                channel.send(null)
            }
        }

        var foundAccount: UserAccount? = null
        var completedCount = 0
        while (completedCount < candidateServers.size) {
            val acc = channel.receive()
            completedCount++
            if (acc != null) {
                foundAccount = acc
                jobs.forEach { it.cancel() }
                break
            }
        }
        channel.close()

        if (foundAccount != null) {
            Result.success(foundAccount)
        } else {
            Result.failure(
                Exception("Usuário ou senha incorretos. Não foi possível conectar em nenhum dos servidores cadastrados.")
            )
        }
    }

    suspend fun authenticate(rawServerUrl: String, user: String, pass: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        try {
            val trimmedUser = user.trim()
            val trimmedPass = pass.trim()
            val encodedUser = try { URLEncoder.encode(trimmedUser, "UTF-8") } catch (_: Exception) { trimmedUser }
            val encodedPass = try { URLEncoder.encode(trimmedPass, "UTF-8") } catch (_: Exception) { trimmedPass }

            val serverUrl = PresetServers.cleanServerUrl(rawServerUrl)
            val url = "$serverUrl/player_api.php?username=$encodedUser&password=$encodedPass"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Erro HTTP ${response.code}: Servidor indisponível"))
            }

            val bodyString = response.body?.string() ?: return@withContext Result.failure(Exception("Resposta vazia do servidor"))
            val json = JSONObject(bodyString)

            val userInfo = json.optJSONObject("user_info")
            if (userInfo == null || !isAccountAuthenticated(userInfo)) {
                val message = json.optString("message", "Usuário ou senha inválidos ou conta expirada")
                return@withContext Result.failure(Exception(message))
            }

            val expTimestampStr = userInfo.optString("exp_date", "")
            val expDateFormatted = formatTimestamp(expTimestampStr)

            val account = UserAccount(
                username = userInfo.optString("username", trimmedUser),
                password = trimmedPass,
                serverUrl = serverUrl,
                serverName = PresetServers.getServerDisplayName(serverUrl),
                expDate = expDateFormatted,
                status = userInfo.optString("status", "Ativo"),
                maxConnections = userInfo.optString("max_connections", "1"),
                activeConnections = userInfo.optString("active_cons", "0"),
                isTrial = userInfo.optString("is_trial", "0") == "1",
                isM3uMode = false
            )
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLiveCategories(serverUrl: String, user: String, pass: String): List<CategoryItem> = withContext(Dispatchers.IO) {
        val cleanUrl = PresetServers.cleanServerUrl(serverUrl)
        val url = "$cleanUrl/player_api.php?username=$user&password=$pass&action=get_live_categories"
        try {
            val response = executeGet(url)
            val jsonArray = JSONArray(response)
            val list = mutableListOf<CategoryItem>()
            list.add(CategoryItem(id = "all", name = "Todos os Canais", type = CategoryType.LIVE))
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                list.add(
                    CategoryItem(
                        id = item.optString("category_id"),
                        name = item.optString("category_name"),
                        type = CategoryType.LIVE
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLiveStreams(
        serverUrl: String,
        user: String,
        pass: String,
        categoryId: String? = null
    ): List<ChannelItem> = withContext(Dispatchers.IO) {
        val cleanUrl = PresetServers.cleanServerUrl(serverUrl)
        val catParam = if (!categoryId.isNullOrBlank() && categoryId != "all") "&category_id=$categoryId" else ""
        val url = "$cleanUrl/player_api.php?username=$user&password=$pass&action=get_live_streams$catParam"
        try {
            val response = executeGet(url)
            val jsonArray = JSONArray(response)
            val list = mutableListOf<ChannelItem>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val streamId = item.optString("stream_id")
                val streamUrl = "$cleanUrl/live/$user/$pass/$streamId.ts"
                list.add(
                    ChannelItem(
                        id = streamId,
                        name = item.optString("name", "Canal $streamId"),
                        streamIcon = item.optString("stream_icon").takeIf { it.isNotBlank() },
                        streamUrl = streamUrl,
                        categoryId = item.optString("category_id"),
                        num = item.optInt("num", i + 1),
                        epgChannelId = item.optString("epg_channel_id").takeIf { it.isNotBlank() }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVodCategories(serverUrl: String, user: String, pass: String): List<CategoryItem> = withContext(Dispatchers.IO) {
        val cleanUrl = PresetServers.cleanServerUrl(serverUrl)
        val url = "$cleanUrl/player_api.php?username=$user&password=$pass&action=get_vod_categories"
        try {
            val response = executeGet(url)
            val jsonArray = JSONArray(response)
            val list = mutableListOf<CategoryItem>()
            list.add(CategoryItem(id = "all", name = "Todos os Filmes", type = CategoryType.MOVIE))
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                list.add(
                    CategoryItem(
                        id = item.optString("category_id"),
                        name = item.optString("category_name"),
                        type = CategoryType.MOVIE
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVodStreams(
        serverUrl: String,
        user: String,
        pass: String,
        categoryId: String? = null
    ): List<MovieItem> = withContext(Dispatchers.IO) {
        val cleanUrl = PresetServers.cleanServerUrl(serverUrl)
        val catParam = if (!categoryId.isNullOrBlank() && categoryId != "all") "&category_id=$categoryId" else ""
        val url = "$cleanUrl/player_api.php?username=$user&password=$pass&action=get_vod_streams$catParam"
        try {
            val response = executeGet(url)
            val jsonArray = JSONArray(response)
            val list = mutableListOf<MovieItem>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val streamId = item.optString("stream_id")
                val ext = item.optString("container_extension", "mp4").ifBlank { "mp4" }
                val streamUrl = "$cleanUrl/movie/$user/$pass/$streamId.$ext"
                list.add(
                    MovieItem(
                        id = streamId,
                        name = item.optString("name", "Filme $streamId"),
                        streamIcon = item.optString("stream_icon").takeIf { it.isNotBlank() },
                        streamUrl = streamUrl,
                        categoryId = item.optString("category_id"),
                        rating = item.optString("rating").takeIf { it.isNotBlank() },
                        releaseDate = item.optString("releasedate").takeIf { it.isNotBlank() },
                        plot = item.optString("plot").takeIf { it.isNotBlank() },
                        containerExtension = ext
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSeriesCategories(serverUrl: String, user: String, pass: String): List<CategoryItem> = withContext(Dispatchers.IO) {
        val cleanUrl = PresetServers.cleanServerUrl(serverUrl)
        val url = "$cleanUrl/player_api.php?username=$user&password=$pass&action=get_series_categories"
        try {
            val response = executeGet(url)
            val jsonArray = JSONArray(response)
            val list = mutableListOf<CategoryItem>()
            list.add(CategoryItem(id = "all", name = "Todas as Séries", type = CategoryType.SERIES))
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                list.add(
                    CategoryItem(
                        id = item.optString("category_id"),
                        name = item.optString("category_name"),
                        type = CategoryType.SERIES
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSeries(
        serverUrl: String,
        user: String,
        pass: String,
        categoryId: String? = null
    ): List<SeriesItem> = withContext(Dispatchers.IO) {
        val cleanUrl = PresetServers.cleanServerUrl(serverUrl)
        val catParam = if (!categoryId.isNullOrBlank() && categoryId != "all") "&category_id=$categoryId" else ""
        val url = "$cleanUrl/player_api.php?username=$user&password=$pass&action=get_series$catParam"
        try {
            val response = executeGet(url)
            val jsonArray = JSONArray(response)
            val list = mutableListOf<SeriesItem>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val seriesId = item.optString("series_id")
                list.add(
                    SeriesItem(
                        id = seriesId,
                        name = item.optString("name", "Série $seriesId"),
                        cover = item.optString("cover").takeIf { it.isNotBlank() },
                        categoryId = item.optString("category_id"),
                        rating = item.optString("rating").takeIf { it.isNotBlank() },
                        releaseDate = item.optString("releaseDate").takeIf { it.isNotBlank() },
                        plot = item.optString("plot").takeIf { it.isNotBlank() }
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSeriesDetails(
        serverUrl: String,
        user: String,
        pass: String,
        series: SeriesItem
    ): SeriesDetails = withContext(Dispatchers.IO) {
        val cleanUrl = PresetServers.cleanServerUrl(serverUrl)
        val url = "$cleanUrl/player_api.php?username=$user&password=$pass&action=get_series_info&series_id=${series.id}"
        try {
            val response = executeGet(url)
            val json = JSONObject(response)

            val seasonsList = mutableListOf<SeasonItem>()
            val seasonsJsonArray = json.optJSONArray("seasons")
            if (seasonsJsonArray != null) {
                for (i in 0 until seasonsJsonArray.length()) {
                    val sObj = seasonsJsonArray.getJSONObject(i)
                    val sNum = sObj.optInt("season_number", i + 1)
                    seasonsList.add(
                        SeasonItem(
                            seasonNumber = sNum,
                            name = sObj.optString("name", "Temporada $sNum"),
                            episodeCount = sObj.optInt("episode_count", 0)
                        )
                    )
                }
            }

            val episodesMap = mutableMapOf<Int, List<EpisodeItem>>()
            val episodesObj = json.optJSONObject("episodes")
            if (episodesObj != null) {
                val keys = episodesObj.keys()
                while (keys.hasNext()) {
                    val seasonKey = keys.next()
                    val seasonNum = seasonKey.toIntOrNull() ?: 1
                    val epArray = episodesObj.optJSONArray(seasonKey)
                    if (epArray != null) {
                        val epList = mutableListOf<EpisodeItem>()
                        for (j in 0 until epArray.length()) {
                            val epJson = epArray.getJSONObject(j)
                            val epId = epJson.optString("id")
                            val ext = epJson.optString("container_extension", "mp4").ifBlank { "mp4" }
                            val epStreamUrl = "$cleanUrl/series/$user/$pass/$epId.$ext"
                            epList.add(
                                EpisodeItem(
                                    id = epId,
                                    episodeNum = epJson.optInt("episode_num", j + 1),
                                    seasonNum = seasonNum,
                                    title = epJson.optString("title", "Episódio ${j + 1}"),
                                    containerExtension = ext,
                                    streamUrl = epStreamUrl,
                                    duration = epJson.optString("duration").takeIf { it.isNotBlank() },
                                    plot = epJson.optString("plot").takeIf { it.isNotBlank() },
                                    cover = epJson.optJSONObject("info")?.optString("movie_image")
                                )
                            )
                        }
                        episodesMap[seasonNum] = epList
                    }
                }
            }

            if (seasonsList.isEmpty() && episodesMap.isNotEmpty()) {
                episodesMap.keys.sorted().forEach { sNum ->
                    seasonsList.add(
                        SeasonItem(
                            seasonNumber = sNum,
                            name = "Temporada $sNum",
                            episodeCount = episodesMap[sNum]?.size ?: 0
                        )
                    )
                }
            }

            SeriesDetails(
                seriesInfo = series,
                seasons = seasonsList.ifEmpty { listOf(SeasonItem(1, "Temporada 1", 1)) },
                episodesBySeason = episodesMap
            )
        } catch (e: Exception) {
            SeriesDetails(seriesInfo = series, seasons = emptyList(), episodesBySeason = emptyMap())
        }
    }

    private fun executeGet(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
        return response.body?.string() ?: ""
    }

    private fun formatTimestamp(timestampStr: String): String {
        return try {
            val ts = timestampStr.toLongOrNull() ?: return timestampStr
            val date = Date(ts * 1000)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(date)
        } catch (e: Exception) {
            timestampStr
        }
    }
}
