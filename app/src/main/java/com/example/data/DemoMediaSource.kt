package com.example.data

import com.example.model.CategoryItem
import com.example.model.CategoryType
import com.example.model.ChannelItem
import com.example.model.EpisodeItem
import com.example.model.MovieItem
import com.example.model.SeasonItem
import com.example.model.SeriesDetails
import com.example.model.SeriesItem

object DemoMediaSource {

    val demoLiveCategories = listOf(
        CategoryItem("cat_all", "Todos os Canais", CategoryType.LIVE),
        CategoryItem("cat_news", "Notícias & Variedades", CategoryType.LIVE),
        CategoryItem("cat_sports", "Esportes & Aventura", CategoryType.LIVE),
        CategoryItem("cat_doc", "Documentários & Ciência", CategoryType.LIVE),
        CategoryItem("cat_kids", "Animações & Infantil", CategoryType.LIVE)
    )

    val demoChannels = listOf(
        ChannelItem(
            id = "demo_live_1",
            name = "NASA TV HD (Ao Vivo)",
            streamIcon = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=400",
            streamUrl = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
            categoryId = "cat_doc",
            categoryName = "Documentários & Ciência",
            num = 1
        ),
        ChannelItem(
            id = "demo_live_2",
            name = "Red Bull TV (Esportes Radicais)",
            streamIcon = "https://images.unsplash.com/photo-1551698618-1dfe5d97d256?w=400",
            streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
            categoryId = "cat_sports",
            categoryName = "Esportes & Aventura",
            num = 2
        ),
        ChannelItem(
            id = "demo_live_3",
            name = "DW News Internacional",
            streamIcon = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=400",
            streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
            categoryId = "cat_news",
            categoryName = "Notícias & Variedades",
            num = 3
        ),
        ChannelItem(
            id = "demo_live_4",
            name = "Bloomberg TV Ao Vivo",
            streamIcon = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=400",
            streamUrl = "https://liveproduseast.global.ssl.fastly.net/btv/desktop/us_live.m3u8",
            categoryId = "cat_news",
            categoryName = "Notícias & Variedades",
            num = 4
        ),
        ChannelItem(
            id = "demo_live_5",
            name = "Big Buck Bunny 24/7",
            streamIcon = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            categoryId = "cat_kids",
            categoryName = "Animações & Infantil",
            num = 5
        )
    )

    val demoMovieCategories = listOf(
        CategoryItem("mov_all", "Todos os Filmes", CategoryType.MOVIE),
        CategoryItem("mov_action", "Ação & Aventura", CategoryType.MOVIE),
        CategoryItem("mov_scifi", "Ficção Científica", CategoryType.MOVIE),
        CategoryItem("mov_anim", "Animação", CategoryType.MOVIE)
    )

    val demoMovies = listOf(
        MovieItem(
            id = "demo_mov_1",
            name = "Tears of Steel (Lágrimas de Aço)",
            streamIcon = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/TearsOfSteel.jpg",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            categoryId = "mov_scifi",
            categoryName = "Ficção Científica",
            rating = "8.4",
            releaseDate = "2024",
            plot = "Num futuro distópico em Amsterdã, um grupo de guerreiros e cientistas tenta salvar o planeta de robôs destrutivos.",
            duration = "12 min",
            containerExtension = "mp4"
        ),
        MovieItem(
            id = "demo_mov_2",
            name = "Sintel - O Dragão Guerreiro",
            streamIcon = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            categoryId = "mov_anim",
            categoryName = "Animação",
            rating = "8.8",
            releaseDate = "2023",
            plot = "Uma jovem guerreira solitária viaja pelas terras perigosas procurando por seu pequeno bebê dragão capturado.",
            duration = "15 min",
            containerExtension = "mp4"
        ),
        MovieItem(
            id = "demo_mov_3",
            name = "Elephants Dream",
            streamIcon = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            categoryId = "mov_scifi",
            categoryName = "Ficção Científica",
            rating = "7.9",
            releaseDate = "2022",
            plot = "Dois amigos exploram uma máquina gigantesca e surreal que parece ter vida própria.",
            duration = "11 min",
            containerExtension = "mp4"
        ),
        MovieItem(
            id = "demo_mov_4",
            name = "Cosmos Laundromat",
            streamIcon = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4",
            categoryId = "mov_action",
            categoryName = "Ação & Aventura",
            rating = "8.6",
            releaseDate = "2024",
            plot = "Uma ovelha deprimida encontra um misterioso vendedor que oferece infinitas vidas em dimensões alternativas.",
            duration = "12 min",
            containerExtension = "mp4"
        )
    )

    val demoSeriesCategories = listOf(
        CategoryItem("ser_all", "Todas as Séries", CategoryType.SERIES),
        CategoryItem("ser_drama", "Ação & Drama", CategoryType.SERIES),
        CategoryItem("ser_doc", "Natureza & Espaço", CategoryType.SERIES)
    )

    val demoSeries = listOf(
        SeriesItem(
            id = "demo_ser_1",
            name = "Crônicas do Espaço Profundo",
            cover = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=400",
            categoryId = "ser_doc",
            categoryName = "Natureza & Espaço",
            rating = "9.1",
            releaseDate = "2024",
            plot = "Uma exploração cinematográfica das luas e planetas mais remotos do nosso sistema solar."
        ),
        SeriesItem(
            id = "demo_ser_2",
            name = "Velocidade Extrema Brasil",
            cover = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=400",
            categoryId = "ser_drama",
            categoryName = "Ação & Drama",
            rating = "8.9",
            releaseDate = "2024",
            plot = "Pilotos de alta performance competindo nas pistas mais desafiadoras da América do Sul."
        )
    )

    fun getDemoSeriesDetails(seriesId: String): SeriesDetails {
        val series = demoSeries.find { it.id == seriesId } ?: demoSeries.first()
        val seasons = listOf(
            SeasonItem(1, "Temporada 1", episodeCount = 3),
            SeasonItem(2, "Temporada 2", episodeCount = 2)
        )
        val ep1 = listOf(
            EpisodeItem(
                id = "ep_1_1",
                episodeNum = 1,
                seasonNum = 1,
                title = "Episódio 1 - A Chegada",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                duration = "15 min",
                plot = "Primeiro contato com as novas fronteiras."
            ),
            EpisodeItem(
                id = "ep_1_2",
                episodeNum = 2,
                seasonNum = 1,
                title = "Episódio 2 - Travessia Noturna",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                duration = "15 min",
                plot = "Desafios em meio à escuridão desconhecida."
            ),
            EpisodeItem(
                id = "ep_1_3",
                episodeNum = 3,
                seasonNum = 1,
                title = "Episódio 3 - O Resgate",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                duration = "14 min",
                plot = "Uma missão de salvamento crítica."
            )
        )
        val ep2 = listOf(
            EpisodeItem(
                id = "ep_2_1",
                episodeNum = 1,
                seasonNum = 2,
                title = "Episódio 1 - Novo Horizonte",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
                duration = "16 min",
                plot = "Um novo mistério surge."
            ),
            EpisodeItem(
                id = "ep_2_2",
                episodeNum = 2,
                seasonNum = 2,
                title = "Episódio 2 - Confronto Final",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                duration = "18 min",
                plot = "A decisão que mudará tudo."
            )
        )
        return SeriesDetails(
            seriesInfo = series,
            seasons = seasons,
            episodesBySeason = mapOf(1 to ep1, 2 to ep2)
        )
    }
}
