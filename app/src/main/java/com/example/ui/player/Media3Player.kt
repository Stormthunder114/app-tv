package com.example.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.ScreenRotationAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.model.ChannelItem
import com.example.model.EpisodeItem
import com.example.model.PlayableMedia
import com.example.model.SeriesDetails
import com.example.model.SeriesItem
import kotlinx.coroutines.delay

private val NetflixRed = Color(0xFFE50914)
private val NetflixDarkBg = Color(0xF50D0D0D)
private val NetflixCardBg = Color(0xF2181818)

enum class VideoResizeMode(val label: String, val mode: Int) {
    FIT("Ajustar", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    FILL("Preencher", AspectRatioFrameLayout.RESIZE_MODE_FILL),
    ZOOM("Zoom", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    FIXED_16_9("16:9", AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH)
}

enum class PlayerOrientation(
    val title: String,
    val shortLabel: String,
    val description: String,
    val requestedOrientation: Int,
    val isLandscapeType: Boolean
) {
    LANDSCAPE_SENSOR(
        title = "Paisagem (Sensor)",
        shortLabel = "Paisagem",
        description = "Gira horizontalmente conforme a posição do aparelho",
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
        isLandscapeType = true
    ),
    PORTRAIT(
        title = "Retrato (Vertical)",
        shortLabel = "Retrato",
        description = "Modo em pé para segurar com facilidade em uma mão",
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
        isLandscapeType = false
    ),
    REVERSE_LANDSCAPE(
        title = "Girar 180° (Invertido)",
        shortLabel = "180°",
        description = "Inverte o sentido horizontal para fone ou carregador",
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
        isLandscapeType = true
    ),
    FULL_SENSOR(
        title = "Automático (360°)",
        shortLabel = "Auto 360°",
        description = "Acompanha os sensores do celular em todos os lados",
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR,
        isLandscapeType = true
    )
}

@OptIn(UnstableApi::class)
@Composable
fun Media3Player(
    media: PlayableMedia,
    channels: List<ChannelItem> = emptyList(),
    seriesDetails: SeriesDetails? = null,
    onSelectChannel: ((ChannelItem) -> Unit)? = null,
    onSelectEpisode: ((SeriesItem, EpisodeItem) -> Unit)? = null,
    onBack: () -> Unit,
    onNext: (() -> Unit)? = null,
    onPrevious: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var isScreenLocked by remember { mutableStateOf(false) }
    var showUnlockButton by remember { mutableStateOf(false) }

    var isChannelDrawerOpen by remember { mutableStateOf(false) }
    var isEpisodeDrawerOpen by remember { mutableStateOf(false) }
    var resizeModeIndex by remember { mutableIntStateOf(0) }
    val resizeModes = VideoResizeMode.values()

    // Screen rotation state
    var currentOrientation by remember { mutableStateOf(PlayerOrientation.LANDSCAPE_SENSOR) }
    var showRotationDialog by remember { mutableStateOf(false) }
    var orientationNotice by remember { mutableStateOf<String?>(null) }

    fun applyOrientation(newOrientation: PlayerOrientation) {
        currentOrientation = newOrientation
        activity?.requestedOrientation = newOrientation.requestedOrientation
        orientationNotice = "Orientação: ${newOrientation.title}"
    }

    val toggleQuickOrientation = {
        val next = if (currentOrientation.isLandscapeType) {
            PlayerOrientation.PORTRAIT
        } else {
            PlayerOrientation.LANDSCAPE_SENSOR
        }
        applyOrientation(next)
    }

    // Playback speed
    val speedOptions = listOf(1.0f, 1.25f, 1.5f, 2.0f)
    var currentSpeedIndex by remember { mutableIntStateOf(0) }

    // ExoPlayer creation - kept persistent across item switches to avoid view re-inflation
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                playWhenReady = true
            }
    }

    // React to streamUrl changes seamlessly
    LaunchedEffect(media.streamUrl) {
        isBuffering = true
        errorMessage = null
        try {
            val mediaItem = MediaItem.fromUri(Uri.parse(media.streamUrl))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        } catch (e: Exception) {
            errorMessage = "Falha ao carregar transmissão"
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    errorMessage = null
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                errorMessage = "Falha ao reproduzir fluxo (${error.errorCodeName})"
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Keep screen oriented according to user selection
    DisposableEffect(currentOrientation) {
        activity?.requestedOrientation = currentOrientation.requestedOrientation
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Auto-dismiss orientation notice HUD after 2.2 seconds
    LaunchedEffect(orientationNotice) {
        if (orientationNotice != null) {
            delay(2200)
            orientationNotice = null
        }
    }

    // Immersive fullscreen mode
    DisposableEffect(activity) {
        val window = activity?.window
        if (window != null) {
            val insetsController = WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Periodic progress polling for VOD
    LaunchedEffect(isPlaying, media.isLive) {
        if (!media.isLive) {
            while (true) {
                currentPosition = exoPlayer.currentPosition
                totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                delay(500)
            }
        }
    }

    // Netflix Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(isControlsVisible, isPlaying, isChannelDrawerOpen, isEpisodeDrawerOpen, isScreenLocked) {
        if (isControlsVisible && isPlaying && !isChannelDrawerOpen && !isEpisodeDrawerOpen && !isScreenLocked) {
            delay(4000)
            isControlsVisible = false
        }
    }

    // Auto-hide unlock prompt when locked
    LaunchedEffect(showUnlockButton) {
        if (showUnlockButton) {
            delay(3500)
            showUnlockButton = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (isScreenLocked) {
                    showUnlockButton = !showUnlockButton
                } else {
                    isControlsVisible = !isControlsVisible
                    if (isChannelDrawerOpen) isChannelDrawerOpen = false
                    if (isEpisodeDrawerOpen) isEpisodeDrawerOpen = false
                }
            }
    ) {
        // Player Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = resizeModes[resizeModeIndex].mode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = resizeModes[resizeModeIndex].mode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator (Netflix red minimal circle)
        if (isBuffering && errorMessage == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = NetflixRed,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        // Error message & retry
        if (errorMessage != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                color = NetflixCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage ?: "Erro de reprodução",
                        color = NetflixRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Não foi possível reproduzir este título.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NetflixRed,
                        modifier = Modifier.clickable {
                            errorMessage = null
                            isBuffering = true
                            exoPlayer.prepare()
                            exoPlayer.play()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Tentar novamente",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Tentar Novamente",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Screen Lock: Floating Unlock button when locked
        if (isScreenLocked) {
            AnimatedVisibility(
                visible = showUnlockButton,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable {
                        isScreenLocked = false
                        showUnlockButton = false
                        isControlsVisible = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Desbloquear",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tela Bloqueada • Toque para Desbloquear",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Clean Netflix Overlay Controls
        AnimatedVisibility(
            visible = isControlsVisible && !isScreenLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.35f),
                                Color.Black.copy(alpha = 0.90f)
                            )
                        )
                    )
            ) {
                // ==================== TOP BAR (Netflix Clean) ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Media Title and Subtitle
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = media.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val subtitleText = when {
                            media.isLive -> "Ao Vivo • ${media.category ?: "Canal HD"}"
                            media.subtitle != null -> media.subtitle
                            else -> media.category ?: ""
                        }
                        if (subtitleText.isNotBlank()) {
                            Text(
                                text = subtitleText,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Screen Aspect Ratio toggle
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.clickable {
                            resizeModeIndex = (resizeModeIndex + 1) % resizeModes.size
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AspectRatio,
                                contentDescription = "Modo de Tela",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = resizeModes[resizeModeIndex].label,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Quick Rotate Button (1-Toque)
                    IconButton(
                        onClick = toggleQuickOrientation,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (!currentOrientation.isLandscapeType) NetflixRed.copy(alpha = 0.35f)
                                else Color.Black.copy(alpha = 0.4f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (!currentOrientation.isLandscapeType) NetflixRed else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .testTag("player_quick_rotate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenRotation,
                            contentDescription = "Girar Tela (1-Toque)",
                            tint = if (!currentOrientation.isLandscapeType) NetflixRed else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Rotation Options Button (Menu com todos os modos)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .testTag("player_rotation_menu_button")
                            .clickable { showRotationDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (currentOrientation) {
                                    PlayerOrientation.PORTRAIT -> Icons.Default.StayCurrentPortrait
                                    PlayerOrientation.LANDSCAPE_SENSOR -> Icons.Default.StayCurrentLandscape
                                    PlayerOrientation.REVERSE_LANDSCAPE -> Icons.Default.ScreenRotationAlt
                                    PlayerOrientation.FULL_SENSOR -> Icons.Default.ScreenRotation
                                },
                                contentDescription = "Modo de Rotação",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentOrientation.shortLabel,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Screen Lock toggle button
                    IconButton(
                        onClick = {
                            isScreenLocked = true
                            isControlsVisible = false
                            showUnlockButton = false
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Travar Tela",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // ==================== CENTER CONTROLS (Netflix Classic) ====================
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s or Previous Channel
                    if (media.isLive) {
                        if (onPrevious != null) {
                            IconButton(
                                onClick = onPrevious,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "Canal Anterior",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    } else {
                        // Netflix -10s Rewind
                        IconButton(
                            onClick = {
                                val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0L)
                                exoPlayer.seekTo(newPos)
                            },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Voltar 10 segundos",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Main Clean Play/Pause Button (Large, iconic Netflix style)
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                        },
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                            tint = Color.Black,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Forward 10s or Next Channel
                    if (media.isLive) {
                        if (onNext != null) {
                            IconButton(
                                onClick = onNext,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f))
                            ) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "Próximo Canal",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    } else {
                        // Netflix +10s Forward
                        IconButton(
                            onClick = {
                                val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                                exoPlayer.seekTo(newPos)
                            },
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "Avançar 10 segundos",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // ==================== BOTTOM CONTROLS (Netflix Scrubber & Tray) ====================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    if (media.isLive) {
                        // Live indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NetflixRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AO VIVO",
                                color = NetflixRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Transmissão em Direto",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        // Netflix Red Scrubber
                        var sliderPosition by remember { mutableFloatStateOf(0f) }
                        var isDragging by remember { mutableStateOf(false) }

                        val duration = totalDuration.coerceAtLeast(1L)
                        val progress = if (isDragging) sliderPosition else (currentPosition.toFloat() / duration.toFloat())

                        Slider(
                            value = progress.coerceIn(0f, 1f),
                            onValueChange = {
                                isDragging = true
                                sliderPosition = it
                            },
                            onValueChangeFinished = {
                                isDragging = false
                                val target = (sliderPosition * duration).toLong()
                                exoPlayer.seekTo(target)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = NetflixRed,
                                activeTrackColor = NetflixRed,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Timestamps: Elapsed & Remaining (authentic Netflix format)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            val remaining = (totalDuration - currentPosition).coerceAtLeast(0L)
                            Text(
                                text = "-${formatTime(remaining)}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Netflix Bottom Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left group: Velocidade & Lock
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!media.isLive) {
                                // Velocidade button
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.Black.copy(alpha = 0.4f),
                                    modifier = Modifier.clickable {
                                        currentSpeedIndex = (currentSpeedIndex + 1) % speedOptions.size
                                        val speed = speedOptions[currentSpeedIndex]
                                        exoPlayer.setPlaybackSpeed(speed)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Speed,
                                            contentDescription = "Velocidade",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val label = if (speedOptions[currentSpeedIndex] == 1.0f) "1x" else "${speedOptions[currentSpeedIndex]}x"
                                        Text(
                                            text = "Velocidade ($label)",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Girar Tela button in bottom bar
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .testTag("bottom_rotation_button")
                                    .clickable { showRotationDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ScreenRotation,
                                        contentDescription = "Girar Tela",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Girar",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Travar button
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Black.copy(alpha = 0.4f),
                                modifier = Modifier.clickable {
                                    isScreenLocked = true
                                    isControlsVisible = false
                                    showUnlockButton = false
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = "Travar",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Travar",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Right group: Canais (Live) OR Episódios (Series) + Next Ep
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // CANAIS button (when Live TV)
                            if (media.isLive) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isChannelDrawerOpen) NetflixRed else Color.Black.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isChannelDrawerOpen) NetflixRed else Color.White.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier.clickable {
                                        isChannelDrawerOpen = !isChannelDrawerOpen
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Menu,
                                            contentDescription = "Lista de Canais",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Canais",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // EPISÓDIOS button (when Series)
                            if (media.isSeries || seriesDetails != null) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isEpisodeDrawerOpen) NetflixRed else Color.Black.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isEpisodeDrawerOpen) NetflixRed else Color.White.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier.clickable {
                                        isEpisodeDrawerOpen = !isEpisodeDrawerOpen
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.VideoLibrary,
                                            contentDescription = "Episódios",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Episódios",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Quick Next Episode Button (Netflix style)
                                if (onNext != null) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.Black.copy(alpha = 0.5f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                        modifier = Modifier.clickable { onNext() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Próx. Ep.",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.SkipNext,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
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

        // ==================== ORIENTATION NOTICE HUD BANNER ====================
        AnimatedVisibility(
            visible = orientationNotice != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 68.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetflixRed.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = NetflixRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = orientationNotice ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==================== NETFLIX EPISODES DRAWER (quando for séries) ====================
        AnimatedVisibility(
            visible = isEpisodeDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(if (!currentOrientation.isLandscapeType) 0.94f else 0.44f)
                .widthIn(max = 420.dp)
                .padding(8.dp)
        ) {
            NetflixEpisodeDrawer(
                details = seriesDetails,
                currentMedia = media,
                onSelectEpisode = { series, ep ->
                    onSelectEpisode?.invoke(series, ep)
                    isEpisodeDrawerOpen = false
                },
                onClose = { isEpisodeDrawerOpen = false }
            )
        }

        // ==================== NETFLIX CHANNELS DRAWER (quando for canais) ====================
        AnimatedVisibility(
            visible = isChannelDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .fillMaxWidth(if (!currentOrientation.isLandscapeType) 0.94f else 0.40f)
                .widthIn(max = 380.dp)
                .padding(8.dp)
        ) {
            NetflixChannelDrawer(
                channels = channels,
                currentMedia = media,
                onSelectChannel = { ch ->
                    onSelectChannel?.invoke(ch)
                    isChannelDrawerOpen = false
                },
                onPrevious = onPrevious,
                onNext = onNext,
                onClose = { isChannelDrawerOpen = false }
            )
        }

        // ==================== ROTATION SELECTOR DIALOG ====================
        if (showRotationDialog) {
            NetflixRotationDialog(
                currentOrientation = currentOrientation,
                onSelectOrientation = { chosen ->
                    applyOrientation(chosen)
                    showRotationDialog = false
                },
                onDismiss = { showRotationDialog = false }
            )
        }
    }
}

/**
 * Netflix-Style Series Episode Drawer
 */
@Composable
private fun NetflixEpisodeDrawer(
    details: SeriesDetails?,
    currentMedia: PlayableMedia,
    onSelectEpisode: (SeriesItem, EpisodeItem) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NetflixDarkBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxSize()
    ) {
        if (details == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NetflixRed, strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Carregando episódios...", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            return@Surface
        }

        val series = details.seriesInfo
        val seasons = details.seasons.ifEmpty {
            listOf(com.example.model.SeasonItem(1, "Temporada 1", details.episodesBySeason[1]?.size ?: 0))
        }
        var selectedSeasonNum by remember {
            mutableIntStateOf(currentMedia.seasonNum.takeIf { it in seasons.map { s -> s.seasonNumber } } ?: seasons.first().seasonNumber)
        }
        val episodes = details.episodesBySeason[selectedSeasonNum] ?: emptyList()

        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = series.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Episódios",
                        color = NetflixRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Season Selector (Pills)
            if (seasons.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(seasons) { season ->
                        val isSelected = season.seasonNumber == selectedSeasonNum
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) NetflixRed else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.clickable { selectedSeasonNum = season.seasonNumber }
                        ) {
                            Text(
                                text = "T${season.seasonNumber}",
                                color = Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Episode List
            if (episodes.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum episódio encontrado",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            } else {
                val currentIdx = episodes.indexOfFirst {
                    it.id == currentMedia.id || (it.seasonNum == currentMedia.seasonNum && it.episodeNum == currentMedia.episodeNum)
                }
                val listState = rememberLazyListState(
                    initialFirstVisibleItemIndex = if (currentIdx > 0) currentIdx else 0
                )

                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(episodes, key = { it.id }) { ep ->
                        val isCurrent = ep.id == currentMedia.id || (ep.seasonNum == currentMedia.seasonNum && ep.episodeNum == currentMedia.episodeNum)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCurrent) NetflixRed.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrent) NetflixRed else Color.White.copy(alpha = 0.08f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectEpisode(series, ep) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail / Cover
                                Box(
                                    modifier = Modifier
                                        .size(width = 64.dp, height = 42.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val cover = ep.cover ?: series.cover
                                    if (!cover.isNullOrBlank()) {
                                        AsyncImage(
                                            model = cover,
                                            contentDescription = ep.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    // Play icon overlay
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) NetflixRed else Color.Black.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${ep.episodeNum}. ${ep.title}",
                                            color = if (isCurrent) NetflixRed else Color.White,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (ep.duration != null) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = ep.duration,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    if (!ep.plot.isNullOrBlank()) {
                                        Text(
                                            text = ep.plot,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 10.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NetflixRed
                                    ) {
                                        Text(
                                            text = "NO AR",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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

/**
 * Netflix-Style Live TV Channels Drawer
 */
@Composable
private fun NetflixChannelDrawer(
    channels: List<ChannelItem>,
    currentMedia: PlayableMedia,
    onSelectChannel: (ChannelItem) -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredChannels = remember(channels, searchQuery) {
        if (searchQuery.isBlank()) channels
        else channels.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.categoryName.contains(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NetflixDarkBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Guia de Canais",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${filteredChannels.size} canais disponíveis",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar canal...", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NetflixRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Next / Previous channel buttons
            if (onPrevious != null || onNext != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onPrevious != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onPrevious() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Anterior", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (onNext != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NetflixRed.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NetflixRed.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNext() }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Próximo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Channel List
            if (filteredChannels.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum canal encontrado",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            } else {
                val currentIdx = filteredChannels.indexOfFirst { it.id == currentMedia.id || it.streamUrl == currentMedia.streamUrl }
                val listState = rememberLazyListState(
                    initialFirstVisibleItemIndex = if (currentIdx > 0) currentIdx else 0
                )

                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(filteredChannels, key = { it.id }) { ch ->
                        val isSelected = ch.id == currentMedia.id || ch.streamUrl == currentMedia.streamUrl
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NetflixRed.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NetflixRed else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectChannel(ch) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!ch.streamIcon.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ch.streamIcon,
                                        contentDescription = ch.name,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.3f))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) NetflixRed else Color.White.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tv,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ch.name,
                                        color = if (isSelected) NetflixRed else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = ch.categoryName,
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }

                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = NetflixRed
                                    ) {
                                        Text(
                                            text = "NO AR",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Netflix-Style Screen Rotation Mode Selector Dialog
 */
@Composable
private fun NetflixRotationDialog(
    currentOrientation: PlayerOrientation,
    onSelectOrientation: (PlayerOrientation) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 320.dp, max = 460.dp)
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent click through */ },
                color = NetflixCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Dialog Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NetflixRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = null,
                                    tint = NetflixRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Rotação de Tela",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Escolha como deseja visualizar o vídeo",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Toggle Button: Alternar rápido entre Horizontal e Vertical
                    val targetQuickToggle = if (currentOrientation.isLandscapeType) PlayerOrientation.PORTRAIT else PlayerOrientation.LANDSCAPE_SENSOR
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NetflixRed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectOrientation(targetQuickToggle)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentOrientation.isLandscapeType) "Girar para Modo Retrato (Vertical)" else "Girar para Modo Paisagem (Horizontal)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "MODOS DE ORIENTAÇÃO",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // List of all orientation options
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        items(PlayerOrientation.values()) { orientationOption ->
                            val isSelected = currentOrientation == orientationOption
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) NetflixRed.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) NetflixRed else Color.White.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectOrientation(orientationOption)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) NetflixRed else Color.White.copy(alpha = 0.1f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (orientationOption) {
                                                PlayerOrientation.PORTRAIT -> Icons.Default.StayCurrentPortrait
                                                PlayerOrientation.LANDSCAPE_SENSOR -> Icons.Default.StayCurrentLandscape
                                                PlayerOrientation.REVERSE_LANDSCAPE -> Icons.Default.ScreenRotationAlt
                                                PlayerOrientation.FULL_SENSOR -> Icons.Default.ScreenRotation
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = orientationOption.title,
                                            color = if (isSelected) NetflixRed else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = orientationOption.description,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selecionado",
                                            tint = NetflixRed,
                                            modifier = Modifier.size(18.dp)
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

