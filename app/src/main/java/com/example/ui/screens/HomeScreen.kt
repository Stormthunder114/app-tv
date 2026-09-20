package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppTab
import com.example.model.ChannelItem
import com.example.model.MovieItem
import com.example.model.SeriesItem
import com.example.model.UserAccount
import com.example.ui.payment.MercadoPagoBlue
import com.example.ui.payment.PixGreen
import com.example.ui.payment.SubscriptionRenewalDialog
import com.example.ui.theme.AssistBgDark
import com.example.ui.theme.AssistBlue
import com.example.ui.theme.AssistCardBorder
import com.example.ui.theme.AssistCardSurface
import com.example.ui.theme.AssistCyan
import com.example.ui.theme.AssistGold
import com.example.ui.theme.AssistRed
import com.example.ui.theme.AssistSurface
import com.example.ui.theme.AssistTextMuted
import com.example.ui.theme.AssistTextPrimary
import com.example.ui.theme.AssistTextSecondary
import com.example.ui.theme.AssistViolet

@Composable
fun HomeScreen(
    account: UserAccount,
    savedAccounts: List<UserAccount> = emptyList(),
    channels: List<ChannelItem>,
    movies: List<MovieItem>,
    series: List<SeriesItem>,
    favoritesCount: Int,
    onNavigateTab: (AppTab) -> Unit,
    onPlayChannel: (ChannelItem) -> Unit,
    onOpenMovie: (MovieItem) -> Unit,
    onOpenSeries: (SeriesItem) -> Unit,
    onSelectSavedAccount: ((UserAccount) -> Unit)? = null,
    onRenewSubscription: ((newExpDate: String) -> Unit)? = null,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenewalDialog by remember { mutableStateOf(false) }

    if (showRenewalDialog) {
        SubscriptionRenewalDialog(
            currentUsername = account.username,
            onRenewalSuccess = { newExpDate ->
                onRenewSubscription?.invoke(newExpDate)
            },
            onDismiss = { showRenewalDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AssistBgDark)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(AssistCyan, AssistViolet))
                        )
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AssistSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Assist",
                            color = AssistTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = " +",
                            color = AssistCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = if (account.username.isNotBlank() && account.username != "M3U Playlist") "Olá, ${account.username}" else "TV & Streaming",
                        color = AssistTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Status chip (clicável para renovar)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F2E22),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                modifier = Modifier.clickable { showRenewalDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (account.expDate.isNotBlank()) "Exp: ${account.expDate}" else "Ativo",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Botão Renovar Assinatura (Mercado Pago QR Code)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MercadoPagoBlue.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MercadoPagoBlue.copy(alpha = 0.6f)),
                modifier = Modifier
                    .clickable { showRenewalDialog = true }
                    .testTag("renew_subscription_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = MercadoPagoBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Renovar",
                        color = MercadoPagoBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Logout / Switch button
            IconButton(
                onClick = onLogout,
                modifier = Modifier.testTag("logout_button")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Trocar Servidor / Sair",
                    tint = AssistTextSecondary
                )
            }
        }

        // Outras Contas Salvas
        val otherAccounts = remember(savedAccounts, account.username) {
            savedAccounts.filter { !it.isM3uMode && it.username != account.username && it.username != "M3U Playlist" }
        }
        if (otherAccounts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    tint = AssistCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Outras contas salvas:",
                    color = AssistTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(otherAccounts) { acc ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = AssistSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
                            modifier = Modifier.clickable {
                                onSelectSavedAccount?.invoke(acc)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AssistCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = acc.username,
                                    color = AssistTextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Banner Renovação Mercado Pago Pix
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF002235),
            border = androidx.compose.foundation.BorderStroke(1.dp, MercadoPagoBlue.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable { showRenewalDialog = true }
                .testTag("home_mercadopago_banner")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MercadoPagoBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Compre ou Renove sua Assinatura",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PixGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PIX",
                                color = PixGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Acesso imediato com QR Code Mercado Pago",
                        color = AssistTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MercadoPagoBlue
                ) {
                    Text(
                        text = "PAGAR",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4 Main Feature Cards (Canais, Filmes, Séries, Favoritos)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Canais de TV & Filmes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Canais de TV Card
                HeroDashboardCard(
                    title = "CANAIS DE TV",
                    subtitle = "${channels.size} canais disponíveis",
                    icon = Icons.Default.LiveTv,
                    gradient = listOf(Color(0xFF0052D4), Color(0xFF4364F7), Color(0xFF6FB1FC)),
                    accentColor = AssistCyan,
                    badgeText = "AO VIVO",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_channels_card"),
                    onClick = { onNavigateTab(AppTab.LIVE_TV) }
                )

                // Filmes Card
                HeroDashboardCard(
                    title = "FILMES",
                    subtitle = "${movies.size} títulos em catálogo",
                    icon = Icons.Default.Movie,
                    gradient = listOf(Color(0xFF7B1FA2), Color(0xFF9C27B0), Color(0xFFBA68C8)),
                    accentColor = AssistViolet,
                    badgeText = "VOD",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_movies_card"),
                    onClick = { onNavigateTab(AppTab.MOVIES) }
                )
            }

            // Row 2: Séries & Favoritos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Séries Card
                HeroDashboardCard(
                    title = "SÉRIES",
                    subtitle = "${series.size} séries completas",
                    icon = Icons.Default.VideoLibrary,
                    gradient = listOf(Color(0xFF007991), Color(0xFF78ffd6)),
                    accentColor = Color(0xFF78ffd6),
                    badgeText = "EPISÓDIOS",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_series_card"),
                    onClick = { onNavigateTab(AppTab.SERIES) }
                )

                // Favoritos Card
                HeroDashboardCard(
                    title = "FAVORITOS",
                    subtitle = "$favoritesCount itens salvos",
                    icon = Icons.Default.Star,
                    gradient = listOf(Color(0xFFE65100), Color(0xFFFF9800), Color(0xFFFFB74D)),
                    accentColor = AssistGold,
                    badgeText = "SALVOS",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_favorites_card"),
                    onClick = { onNavigateTab(AppTab.FAVORITES) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Canais Populares Ao Vivo
        if (channels.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AssistRed, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Canais em Destaque",
                        color = AssistTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Ver Todos",
                    color = AssistCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateTab(AppTab.LIVE_TV) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(channels.take(8)) { channel ->
                    LiveChannelCard(
                        channel = channel,
                        onClick = { onPlayChannel(channel) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Filmes em Alta
        if (movies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filmes em Destaque",
                    color = AssistTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ver Todos",
                    color = AssistCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateTab(AppTab.MOVIES) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(movies.take(6)) { movie ->
                    MoviePosterCard(
                        movie = movie,
                        onClick = { onOpenMovie(movie) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Séries Populares
        if (series.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Séries em Destaque",
                    color = AssistTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ver Todas",
                    color = AssistCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateTab(AppTab.SERIES) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(series.take(6)) { ser ->
                    SeriesPosterCard(
                        series = ser,
                        onClick = { onOpenSeries(ser) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Server Info Panel
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            color = AssistSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = AssistCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Informações do Servidor",
                        color = AssistTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Servidor:", color = AssistTextMuted, fontSize = 12.sp)
                    Text(
                        account.serverUrl.ifBlank { "Lista M3U" },
                        color = AssistTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Usuário:", color = AssistTextMuted, fontSize = 12.sp)
                    Text(
                        account.username.ifBlank { "Convidado" },
                        color = AssistCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Conexões:", color = AssistTextMuted, fontSize = 12.sp)
                    Text(
                        "${account.activeConnections} / ${account.maxConnections}",
                        color = AssistTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HeroDashboardCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    accentColor: Color,
    badgeText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AssistCardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            gradient[0].copy(alpha = 0.35f),
                            AssistCardSurface
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(accentColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = badgeText,
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = title,
                    color = AssistTextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = AssistTextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LiveChannelCard(
    channel: ChannelItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AssistCardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AssistSurface),
                contentAlignment = Alignment.Center
            ) {
                if (!channel.streamIcon.isNullOrBlank()) {
                    AsyncImage(
                        model = channel.streamIcon,
                        contentDescription = channel.name,
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = AssistCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = channel.name,
                color = AssistTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = channel.categoryName,
                color = AssistTextMuted,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MoviePosterCard(
    movie: MovieItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AssistCardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
        modifier = modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(AssistSurface)
            ) {
                if (!movie.streamIcon.isNullOrBlank()) {
                    AsyncImage(
                        model = movie.streamIcon,
                        contentDescription = movie.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = AssistViolet, modifier = Modifier.size(36.dp))
                    }
                }

                if (!movie.rating.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AssistGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = movie.rating,
                                color = AssistGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = movie.name,
                    color = AssistTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = movie.releaseDate ?: movie.categoryName,
                    color = AssistTextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SeriesPosterCard(
    series: SeriesItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AssistCardSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
        modifier = modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(AssistSurface)
            ) {
                if (!series.cover.isNullOrBlank()) {
                    AsyncImage(
                        model = series.cover,
                        contentDescription = series.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = AssistCyan, modifier = Modifier.size(36.dp))
                    }
                }

                if (!series.rating.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AssistGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = series.rating,
                                color = AssistGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = series.name,
                    color = AssistTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = series.categoryName,
                    color = AssistTextMuted,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
