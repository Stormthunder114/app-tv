package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AssistBgDark
import com.example.ui.theme.AssistCardBorder
import com.example.ui.theme.AssistCyan
import com.example.ui.theme.AssistSurface
import com.example.ui.theme.AssistTextMuted
import com.example.ui.theme.AssistViolet
import kotlinx.coroutines.delay

@Composable
fun LoadingConnectionScreen(
    message: String = "Conectando aos servidores e usuários...",
    subMessage: String = "Aguarde enquanto carrega",
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_and_rotate")

    // Gentle pulse effect for logo
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    // Glowing halo pulse
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_glow"
    )

    // Rotating ring around logo
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // Dynamic cycling status text
    val statusSteps = listOf(
        "Conectando aos servidores...",
        "Autenticando usuário e conta...",
        "Carregando canais, filmes e séries...",
        "Aguarde enquanto carrega..."
    )
    var currentStepIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1800)
            currentStepIndex = (currentStepIndex + 1) % statusSteps.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AssistBgDark)
            .testTag("loading_connection_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AssistCyan.copy(alpha = haloAlpha * 0.35f),
                            AssistViolet.copy(alpha = haloAlpha * 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Logo Container
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Rotating subtle gradient ring
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .rotate(rotation)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    AssistCyan,
                                    AssistViolet,
                                    Color.Transparent,
                                    AssistCyan
                                )
                            )
                        )
                )

                // Logo inner box
                Box(
                    modifier = Modifier
                        .size(98.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(AssistSurface)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(listOf(AssistCyan, AssistViolet)),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Assist + Logo",
                        modifier = Modifier.size(68.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Title
            Text(
                text = message,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Emphasized Badge: "Aguarde enquanto carrega"
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AssistCyan.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AssistCyan.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = AssistCyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subMessage,
                        color = AssistCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Status Card (Servidores e Usuários)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = AssistSurface.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AssistCardBorder.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Step 1: Servidor
                    ConnectionStepRow(
                        icon = Icons.Default.Dns,
                        title = "Servidores IPTV",
                        status = if (currentStepIndex >= 0) "Conectando..." else "Aguardando",
                        isActive = currentStepIndex == 0
                    )

                    // Step 2: Usuário
                    ConnectionStepRow(
                        icon = Icons.Default.Person,
                        title = "Usuário & Conta",
                        status = if (currentStepIndex >= 1) "Autenticando..." else "Pendente",
                        isActive = currentStepIndex == 1
                    )

                    // Step 3: Conteúdo
                    ConnectionStepRow(
                        icon = Icons.Default.LiveTv,
                        title = "Catálogo & Canais",
                        status = if (currentStepIndex >= 2) "Sincronizando..." else "Pendente",
                        isActive = currentStepIndex >= 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sleek progress bar
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AssistCyan,
                        trackColor = AssistCardBorder
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Cycling message
            Text(
                text = statusSteps[currentStepIndex],
                color = AssistTextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            // Optional Cancel Button
            if (onCancel != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .clickable { onCancel() }
                        .testTag("cancel_loading_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancelar",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cancelar",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionStepRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) AssistCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) AssistCyan else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        Text(
            text = status,
            color = if (isActive) AssistCyan else AssistTextMuted,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
