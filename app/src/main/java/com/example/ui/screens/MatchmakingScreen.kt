package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.LudoPlayer
import com.example.ui.theme.GameBgDeep
import com.example.ui.theme.GameBgSurface

@Composable
fun MatchmakingScreen(
    mode: GameMode,
    matchedPlayers: List<LudoPlayer>,
    progress: Float,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onCancel() }

    val totalSlots = if (mode == GameMode.TWO_PLAYERS) 2 else 4

    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_radar"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameBgDeep, GameBgSurface, Color(0xFF030A12))
                )
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = mode.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prize Pool: ${mode.prizePool} 🪙",
                    color = Color(0xFFFFD54F),
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF37474F), CircleShape)
                    .testTag("cancel_matchmaking_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }
        }

        // Center Radar / Players Slot Grid
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Radar Animation Circle
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(radarPulse)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00E676).copy(alpha = 0.25f),
                                Color(0xFF00E676).copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
                    .border(2.dp, Color(0xFF00E676).copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF0D2D44), CircleShape)
                        .border(2.dp, Color(0xFF00E676), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎲", fontSize = 36.sp)
                }
            }

            Text(
                text = "Finding Opponents...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Players Found Grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0 until totalSlots) {
                    val player = matchedPlayers.getOrNull(index)
                    PlayerSlotCard(player = player, slotIndex = index + 1)
                }
            }
        }

        // Bottom Progress & Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFF00E676),
                trackColor = Color(0xFF263238),
            )

            Text(
                text = "${matchedPlayers.size} of $totalSlots Players Ready",
                color = Color(0xFF90A4AE),
                fontSize = 13.sp
            )

            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CANCEL SEARCH", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlayerSlotCard(player: LudoPlayer?, slotIndex: Int) {
    Card(
        modifier = Modifier
            .size(72.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (player != null) Color(0xFF0E314D) else Color(0xFF1E293B).copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (player != null) Color(0xFF00E676) else Color(0xFF475569)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (player != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(player.color.hexColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = player.name.firstOrNull()?.toString() ?: "P",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = player.name,
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Slot $slotIndex",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
