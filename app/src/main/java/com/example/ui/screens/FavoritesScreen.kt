package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChannelItem
import com.example.model.MovieItem
import com.example.model.SeriesItem
import com.example.ui.theme.AssistBgDark
import com.example.ui.theme.AssistCardBorder
import com.example.ui.theme.AssistCardSurface
import com.example.ui.theme.AssistCyan
import com.example.ui.theme.AssistGold
import com.example.ui.theme.AssistSurface
import com.example.ui.theme.AssistTextMuted
import com.example.ui.theme.AssistTextPrimary
import com.example.ui.theme.AssistTextSecondary
import com.example.ui.theme.AssistViolet

@Composable
fun FavoritesScreen(
    channels: List<ChannelItem>,
    movies: List<MovieItem>,
    series: List<SeriesItem>,
    favorites: Set<String>,
    onPlayChannel: (ChannelItem) -> Unit,
    onOpenMovie: (MovieItem) -> Unit,
    onOpenSeries: (SeriesItem) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("all") }

    val favChannels = channels.filter { favorites.contains(it.id) }
    val favMovies = movies.filter { favorites.contains(it.id) }
    val favSeries = series.filter { favorites.contains(it.id) }

    val totalFavorites = favChannels.size + favMovies.size + favSeries.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AssistBgDark)
            .padding(bottom = 80.dp)
    ) {
        // Title bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = AssistGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Meus Favoritos",
                color = AssistTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AssistGold.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$totalFavorites",
                    color = AssistGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChipItem(
                    label = "Todos ($totalFavorites)",
                    isSelected = selectedFilter == "all",
                    onClick = { selectedFilter = "all" }
                )
            }
            item {
                FilterChipItem(
                    label = "Canais (${favChannels.size})",
                    isSelected = selectedFilter == "channels",
                    onClick = { selectedFilter = "channels" }
                )
            }
            item {
                FilterChipItem(
                    label = "Filmes (${favMovies.size})",
                    isSelected = selectedFilter == "movies",
                    onClick = { selectedFilter = "movies" }
                )
            }
            item {
                FilterChipItem(
                    label = "Séries (${favSeries.size})",
                    isSelected = selectedFilter == "series",
                    onClick = { selectedFilter = "series" }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (totalFavorites == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = AssistTextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Nenhum favorito salvo ainda",
                        color = AssistTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Toque na estrela em canais, filmes ou séries para salvar aqui",
                        color = AssistTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Canais
                if (selectedFilter == "all" || selectedFilter == "channels") {
                    items(favChannels, key = { "ch_${it.id}" }) { channel ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AssistCardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPlayChannel(channel) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AssistSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!channel.streamIcon.isNullOrBlank()) {
                                        AsyncImage(
                                            model = channel.streamIcon,
                                            contentDescription = channel.name,
                                            modifier = Modifier.size(40.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Icon(Icons.Default.LiveTv, contentDescription = null, tint = AssistCyan)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = channel.name,
                                        color = AssistTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Canal Ao Vivo • ${channel.categoryName}",
                                        color = AssistTextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(onClick = { onToggleFavorite(channel.id) }) {
                                    Icon(Icons.Default.Star, contentDescription = "Remover", tint = AssistGold)
                                }
                            }
                        }
                    }
                }

                // Filmes
                if (selectedFilter == "all" || selectedFilter == "movies") {
                    items(favMovies, key = { "mov_${it.id}" }) { movie ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AssistCardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenMovie(movie) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AssistSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!movie.streamIcon.isNullOrBlank()) {
                                        AsyncImage(
                                            model = movie.streamIcon,
                                            contentDescription = movie.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Movie, contentDescription = null, tint = AssistViolet)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = movie.name,
                                        color = AssistTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Filme • ${movie.categoryName}",
                                        color = AssistTextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(onClick = { onToggleFavorite(movie.id) }) {
                                    Icon(Icons.Default.Star, contentDescription = "Remover", tint = AssistGold)
                                }
                            }
                        }
                    }
                }

                // Séries
                if (selectedFilter == "all" || selectedFilter == "series") {
                    items(favSeries, key = { "ser_${it.id}" }) { ser ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AssistCardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenSeries(ser) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AssistSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!ser.cover.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ser.cover,
                                            contentDescription = ser.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = AssistCyan)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ser.name,
                                        color = AssistTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Série • ${ser.categoryName}",
                                        color = AssistTextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                IconButton(onClick = { onToggleFavorite(ser.id) }) {
                                    Icon(Icons.Default.Star, contentDescription = "Remover", tint = AssistGold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) AssistCyan else AssistSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AssistCyan else AssistCardBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else AssistTextPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
