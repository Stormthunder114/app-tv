package com.example.data

import com.example.model.ServerConfig

object PresetServers {
    val candidateServerUrls = listOf(
        "http://digitalbr.cloud",
        "http://cdnultra.sbs",
        "http://ltracdn.sbs",
        "http://elitecdn.sbs",
        "http://cdnchurras.space",
        "http://onpix.sbs",
        "http://todeolho.shop",
        "http://telefunplay.xyz",
        "http://cdntopz.xyz",
        "http://dragonbal.space",
        "http://main.alprox.xyz",
        "http://fsaura.sbs",
        "http://lifeyouyes.shop",
        "http://cdn.jfplay.fun",
        "http://cdn.timegs.online:80",
        "http://cdn.timegs.online",
        "http://xerxs.click",
        "http://cinebox.blog",
        "http://e.boss.cdnfk.com.br",
        "http://offtheking.xyz:80",
        "http://offtheking.xyz",
        "http://digitalbr.cloud:80",
        "http://cdnultra.sbs:80",
        "http://ltracdn.sbs:80",
        "http://elitecdn.sbs:80",
        "http://cdnchurras.space:80",
        "http://onpix.sbs:80",
        "http://todeolho.shop:80",
        "http://telefunplay.xyz:80",
        "http://cdntopz.xyz:80",
        "http://dragonbal.space:80",
        "http://main.alprox.xyz:80",
        "http://fsaura.sbs:80",
        "http://lifeyouyes.shop:80",
        "http://cdn.jfplay.fun:80",
        "http://cinebox.blog:80",
        "http://e.boss.cdnfk.com.br:80",
        "http://xerxs.click:80"
    )

    fun getServerDisplayName(cleanUrl: String): String {
        val lower = cleanUrl.lowercase()
        return when {
            "digitalbr" in lower -> "Digital BR"
            "cdnultra" in lower -> "CDN Ultra"
            "ltracdn" in lower -> "Ltra CDN"
            "elitecdn" in lower -> "Elite CDN"
            "churras" in lower -> "CDN Churras"
            "onpix" in lower -> "OnPix"
            "todeolho" in lower -> "Tô De Olho"
            "telefunplay" in lower -> "Telefun Play"
            "cdntopz" in lower -> "CDN Topz"
            "dragonbal" in lower -> "Dragon Ball"
            "alprox" in lower -> "Alprox"
            "fsaura" in lower -> "Fsaura"
            "lifeyouyes" in lower -> "Life You Yes"
            "jfplay" in lower -> "JF Play"
            "timegs" in lower -> "TimeGS"
            "cinebox" in lower -> "Cinebox"
            "boss" in lower || "cdnfk" in lower -> "Boss CDN FK"
            "xerxs" in lower -> "Xerxs Click"
            "offtheking" in lower -> "Off The King"
            else -> defaultServers.find { it.url.equals(cleanUrl, ignoreCase = true) }?.name ?: "Servidor Conectado"
        }
    }

    val defaultServers = listOf(
        ServerConfig(
            name = "Digital BR",
            url = "http://digitalbr.cloud",
            description = "Servidor Digital BR"
        ),
        ServerConfig(
            name = "CDN Ultra",
            url = "http://cdnultra.sbs",
            description = "Servidor CDN Ultra"
        ),
        ServerConfig(
            name = "Ltra CDN",
            url = "http://ltracdn.sbs",
            description = "Servidor Ltra CDN"
        ),
        ServerConfig(
            name = "Elite CDN",
            url = "http://elitecdn.sbs",
            description = "Servidor Elite CDN"
        ),
        ServerConfig(
            name = "CDN Churras",
            url = "http://cdnchurras.space",
            description = "Servidor CDN Churras"
        ),
        ServerConfig(
            name = "OnPix",
            url = "http://onpix.sbs",
            description = "Servidor OnPix"
        ),
        ServerConfig(
            name = "Tô De Olho",
            url = "http://todeolho.shop",
            description = "Servidor Tô De Olho"
        ),
        ServerConfig(
            name = "Telefun Play",
            url = "http://telefunplay.xyz",
            description = "Servidor Telefun Play"
        ),
        ServerConfig(
            name = "CDN Topz",
            url = "http://cdntopz.xyz",
            description = "Servidor CDN Topz"
        ),
        ServerConfig(
            name = "Dragon Ball Space",
            url = "http://dragonbal.space",
            description = "Servidor Dragon Ball Space"
        ),
        ServerConfig(
            name = "Alprox",
            url = "http://main.alprox.xyz",
            description = "Servidor Alprox"
        ),
        ServerConfig(
            name = "Fsaura",
            url = "http://fsaura.sbs",
            description = "Servidor Fsaura"
        ),
        ServerConfig(
            name = "Life You Yes",
            url = "http://lifeyouyes.shop",
            description = "Servidor Life You Yes"
        ),
        ServerConfig(
            name = "JF Play",
            url = "http://cdn.jfplay.fun",
            description = "Servidor JF Play"
        ),
        ServerConfig(
            name = "TimeGS (Porta 80)",
            url = "http://cdn.timegs.online:80",
            description = "Servidor TimeGS"
        ),
        ServerConfig(
            name = "Xerxs Click",
            url = "http://xerxs.click",
            description = "Servidor Xerxs"
        ),
        ServerConfig(
            name = "Cinebox",
            url = "http://cinebox.blog",
            description = "Servidor Cinebox Blog"
        ),
        ServerConfig(
            name = "Boss CDN FK",
            url = "http://e.boss.cdnfk.com.br",
            description = "Servidor CDN FK"
        ),
        ServerConfig(
            name = "Off The King (Porta 80)",
            url = "http://offtheking.xyz:80",
            description = "Servidor OffTheKing Xtream"
        ),
        ServerConfig(
            name = "Outro (Personalizado)",
            url = "",
            description = "Digite o endereço do seu servidor"
        )
    )

    val defaultM3uPresets = listOf(
        "http://offtheking.xyz:80/get.php",
        "http://cinebox.blog/get.php"
    )

    /**
     * Normalizes a server URL by removing double protocols, trailing slashes,
     * or accidentally included get.php / player_api.php paths.
     */
    fun cleanServerUrl(rawUrl: String): String {
        var clean = rawUrl.trim()
        // Fix accidental http://http://
        while (clean.startsWith("http://http://", ignoreCase = true)) {
            clean = "http://" + clean.substring("http://http://".length)
        }
        while (clean.startsWith("https://https://", ignoreCase = true)) {
            clean = "https://" + clean.substring("https://https://".length)
        }
        if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
            clean = "http://$clean"
        }
        // Remove trailing slashes and script paths
        clean = clean.removeSuffix("/")
        if (clean.endsWith("/player_api.php", ignoreCase = true)) {
            clean = clean.substring(0, clean.length - "/player_api.php".length)
        }
        if (clean.endsWith("/get.php", ignoreCase = true)) {
            clean = clean.substring(0, clean.length - "/get.php".length)
        }
        return clean.removeSuffix("/")
    }

    /**
     * Cleans an M3U URL (fixes accidental http://http://)
     */
    fun cleanM3uUrl(rawUrl: String): String {
        var clean = rawUrl.trim()
        while (clean.startsWith("http://http://", ignoreCase = true)) {
            clean = "http://" + clean.substring("http://http://".length)
        }
        while (clean.startsWith("https://https://", ignoreCase = true)) {
            clean = "https://" + clean.substring("https://https://".length)
        }
        if (!clean.startsWith("http://", ignoreCase = true) && !clean.startsWith("https://", ignoreCase = true)) {
            clean = "http://$clean"
        }
        return clean
    }
}
