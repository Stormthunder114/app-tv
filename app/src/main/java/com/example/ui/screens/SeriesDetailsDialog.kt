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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.EpisodeItem
import com.example.model.SeriesDetails
import com.example.model.SeriesItem
import com.example.ui.theme.AssistCardBorder
import com.example.ui.theme.AssistCardSurface
import com.example.ui.theme.AssistCyan
import com.example.ui.theme.AssistGold
import com.example.ui.theme.AssistSurface
import com.example.ui.theme.AssistTextMuted
import com.example.ui.theme.AssistTextPrimary
import com.example.ui.theme.AssistTextSecondary

@Composable
fun SeriesDetailsDialog(
    details: SeriesDetails?,
    isLoading: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit,
    onPlayEpisode: (SeriesItem, EpisodeItem) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AssistCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(580.dp)
                .padding(vertical = 12.dp)
        ) {
            if (isLoading || details == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AssistCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Carregando temporadas...", color = AssistTextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                val series = details.seriesInfo
                var selectedSeasonNum by remember {
                    mutableIntStateOf(details.seasons.firstOrNull()?.seasonNumber ?: 1)
                }

                val currentEpisodes = details.episodesBySeason[selectedSeasonNum] ?: emptyList()

                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(AssistSurface, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = AssistTextSecondary)
                        }

                        Text(
                            text = series.name,
                            color = AssistTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )

                        IconButton(
                            onClick = { onToggleFavorite(series.id) },
                            modifier = Modifier
                                .size(36.dp)
                                .background(AssistSurface, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favoritar",
                                tint = if (isFavorite) AssistGold else AssistTextSecondary
                            )
                        }
                    }

                    // Series Cover & Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AssistSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!series.cover.isNullOrBlank()) {
                                AsyncImage(
                                    model = series.cover,
                                    contentDescription = series.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = AssistCyan, modifier = Modifier.size(36.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!series.rating.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = AssistGold.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = AssistGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(series.rating, color = AssistGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Surface(shape = RoundedCornerShape(4.dp), color = AssistSurface) {
                                    Text(
                                        text = series.categoryName,
                                        color = AssistCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = series.plot?.takeIf { it.isNotBlank() } ?: "Acompanhe todos os episódios completos da série.",
                                color = AssistTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Seasons selector chips
                    if (details.seasons.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(details.seasons) { season ->
                                val isSelected = selectedSeasonNum == season.seasonNumber
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) AssistCyan else AssistSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) AssistCyan else AssistCardBorder
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedSeasonNum = season.seasonNumber }
                                ) {
                                    Text(
                                        text = season.name,
                                        color = if (isSelected) Color.Black else AssistTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Episódios (${currentEpisodes.size})",
                        color = AssistTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Episodes List
                    if (currentEpisodes.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Nenhum episódio disponível para esta temporada", color = AssistTextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(currentEpisodes, key = { it.id }) { ep ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AssistSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onPlayEpisode(series, ep) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(AssistCyan.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AssistCyan, modifier = Modifier.size(20.dp))
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ep.title,
                                                color = AssistTextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (!ep.duration.isNullOrBlank()) {
                                                Text(
                                                    text = ep.duration,
                                                    color = AssistTextMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Ep. ${ep.episodeNum}",
                                            color = AssistCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
