package com.example

import com.example.data.PresetServers
import com.example.network.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCleanServerUrls_handlesAllUserProvidedFormats() {
        assertEquals("http://cinebox.blog", PresetServers.cleanServerUrl("http://cinebox.blog"))
        assertEquals("http://e.boss.cdnfk.com.br", PresetServers.cleanServerUrl("http://e.boss.cdnfk.com.br"))
        assertEquals("http://xerxs.click", PresetServers.cleanServerUrl("http://xerxs.click"))
        assertEquals("http://offtheking.xyz:80", PresetServers.cleanServerUrl("http://http://offtheking.xyz:80/get.php"))
        assertEquals("http://offtheking.xyz:80", PresetServers.cleanServerUrl("http://offtheking.xyz:80/get.php"))
        assertEquals("http://offtheking.xyz:80", PresetServers.cleanServerUrl("offtheking.xyz:80"))
    }

    @Test
    fun testCandidateServers_containsAllRequiredHosts() {
        assertTrue(PresetServers.candidateServerUrls.contains("http://cinebox.blog"))
        assertTrue(PresetServers.candidateServerUrls.contains("http://e.boss.cdnfk.com.br"))
        assertTrue(PresetServers.candidateServerUrls.contains("http://xerxs.click"))
        assertTrue(PresetServers.candidateServerUrls.contains("http://offtheking.xyz:80"))
    }

    @Test
    fun testM3uParser_parsesContentCorrectly() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:-1 tvg-id="globo.br" tvg-name="Globo SP FHD" tvg-logo="http://logo.com/globo.png" group-title="VARIEDADES",Globo SP FHD
            http://cinebox.blog/live/user/pass/101.m3u8
            #EXTINF:-1 tvg-name="Vingadores Ultimato" tvg-logo="http://logo.com/movie.jpg" group-title="FILMES - AÇÃO",Vingadores: Ultimato (2019)
            http://cinebox.blog/movie/user/pass/202.mp4
            #EXTINF:-1 tvg-name="Stranger Things" tvg-logo="http://logo.com/series.jpg" group-title="SÉRIES - DRAMA",Stranger Things S01 E01
            http://cinebox.blog/series/user/pass/303.mp4
        """.trimIndent()

        val result = M3uParser.parseContent(sampleM3u)

        assertEquals(1, result.liveChannels.size)
        assertEquals("Globo SP FHD", result.liveChannels[0].name)
        assertEquals("VARIEDADES", result.liveChannels[0].categoryName)
        assertEquals("http://cinebox.blog/live/user/pass/101.m3u8", result.liveChannels[0].streamUrl)

        assertEquals(1, result.movies.size)
        assertEquals("Vingadores: Ultimato (2019)", result.movies[0].name)
        assertEquals("http://cinebox.blog/movie/user/pass/202.mp4", result.movies[0].streamUrl)

        assertEquals(1, result.series.size)
        assertEquals("Stranger Things S01 E01", result.series[0].name)
    }

    @Test
    fun testMercadoPagoSubscriptionPlans_containValidPricesAndPix() {
        val plans = com.example.ui.payment.defaultSubscriptionPlans
        assertTrue(plans.isNotEmpty())
        assertEquals(4, plans.size)
        assertEquals("42920009293", com.example.ui.payment.USER_PIX_KEY)

        val testPlan = plans.first { it.id == "plan_teste" }
        assertEquals("https://mpago.la/2R4LHGT", testPlan.mpLink)

        val mensalPlan = plans.first { it.id == "plan_mensal" }
        assertEquals("https://mpago.la/2PDbeVp", mensalPlan.mpLink)
        assertEquals("R$ 25,00", mensalPlan.priceFormatted)

        val trimestralPlan = plans.first { it.id == "plan_trimestral" }
        assertEquals("https://mpago.la/1pSFsDj", trimestralPlan.mpLink)
        assertEquals("R$ 65,00", trimestralPlan.priceFormatted)

        val anualPlan = plans.first { it.id == "plan_anual" }
        assertEquals("https://mpago.la/1vFob7q", anualPlan.mpLink)
        assertEquals("R$ 199,00", anualPlan.priceFormatted)

        plans.forEach { plan ->
            assertTrue(plan.title.isNotBlank())
            assertTrue(plan.priceFormatted.isNotBlank())
            assertTrue(plan.durationDays > 0)
            assertTrue(plan.pixCode.isNotBlank())
            assertTrue(plan.pixCode.contains("42920009293"))
            assertTrue(plan.pixCode.startsWith("000201"))
            assertTrue(plan.mpLink.startsWith("https://mpago.la/"))
        }
    }

    @Test
    fun testPixPayloadGenerator_createsValidPixPayload() {
        val pixCode = com.example.util.PixPayloadGenerator.generatePixCode(
            pixKey = "42920009293",
            amount = 25.0
        )
        assertNotNull(pixCode)
        assertTrue(pixCode.startsWith("000201"))
        assertTrue(pixCode.contains("br.gov.bcb.pix"))
        assertTrue(pixCode.contains("42920009293"))
        assertTrue(pixCode.contains("25.00"))
        assertTrue(pixCode.contains("6304"))
    }
}

