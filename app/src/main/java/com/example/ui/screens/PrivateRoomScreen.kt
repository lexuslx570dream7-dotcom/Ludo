package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.LudoPlayer
import com.example.model.PlayerColor
import com.example.ui.components.Glossy3DButton
import com.example.ui.theme.GameBgDeep
import com.example.ui.theme.GameBgSurface

enum class PrivateLocalTab {
    PRIVATE_ROOM,
    LOCAL_PASS_AND_PLAY
}

@Composable
fun PrivateRoomScreen(
    roomCode: String,
    players: List<LudoPlayer>,
    isOwner: Boolean,
    onGenerateNewCode: () -> Unit,
    onAddBotPlayer: () -> Unit,
    onStartGame: () -> Unit,
    onStartLocalMatch: (playerCount: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableStateOf(PrivateLocalTab.PRIVATE_ROOM) }
    var joinCodeInput by remember { mutableStateOf("") }
    var localPlayerCount by remember { mutableStateOf(4) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameBgDeep, GameBgSurface, Color(0xFF030D18))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("🔒 Private & Local", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // Mode Switch: Private Room vs Pass & Play
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D253B), RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { selectedTab = PrivateLocalTab.PRIVATE_ROOM },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == PrivateLocalTab.PRIVATE_ROOM) Color(0xFF00B0FF) else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Private Room", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = { selectedTab = PrivateLocalTab.LOCAL_PASS_AND_PLAY },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == PrivateLocalTab.LOCAL_PASS_AND_PLAY) Color(0xFF00B0FF) else Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Pass & Play", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        when (selectedTab) {
            PrivateLocalTab.PRIVATE_ROOM -> {
                // Room Code Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFF00B0FF), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2238)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Room Invite Code", color = Color(0xFF90A4AE), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = roomCode,
                            color = Color(0xFFFFD54F),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onGenerateNewCode,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("New Code", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { /* Share simulated */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Players in Lobby
                Text(
                    text = "Room Players (${players.size}/4)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in 0..3) {
                        val player = players.getOrNull(i)
                        LobbyPlayerSlot(player = player, slotNum = i + 1)
                    }
                }

                // Action buttons
                if (players.size < 4) {
                    Button(
                        onClick = onAddBotPlayer,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E466A)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add AI Player to Slot", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Glossy3DButton(
                    text = if (players.size >= 2) "START MATCH" else "NEED AT LEAST 2 PLAYERS",
                    icon = "⚔️",
                    onClick = onStartGame,
                    enabled = players.size >= 2,
                    gradientColors = if (players.size >= 2) listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF00796B)) else listOf(Color(0xFF546E7A), Color(0xFF37474F)),
                    depthColor = if (players.size >= 2) Color(0xFF004D40) else Color(0xFF263238),
                    fontSize = 15,
                    testTag = "start_private_room_game_btn"
                )

                HorizontalDivider(color = Color(0xFF1E3A52), modifier = Modifier.padding(vertical = 8.dp))

                // Join Room with Code
                Text(
                    text = "Join Another Private Room",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = joinCodeInput,
                        onValueChange = { joinCodeInput = it.uppercase() },
                        placeholder = { Text("e.g. LD-123456", color = Color(0xFF78909C)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00B0FF),
                            unfocusedBorderColor = Color(0xFF37474F)
                        )
                    )

                    Glossy3DButton(
                        text = "JOIN",
                        onClick = onStartGame,
                        gradientColors = listOf(Color(0xFF00B0FF), Color(0xFF0288D1), Color(0xFF01579B)),
                        depthColor = Color(0xFF0D47A1),
                        fontSize = 13,
                        modifier = Modifier.width(90.dp),
                        testTag = "join_private_room_btn"
                    )
                }
            }

            PrivateLocalTab.LOCAL_PASS_AND_PLAY -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2238)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("🎮 Local Pass & Play", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Play on this single Android device with friends and family without internet!",
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp
                        )

                        Text("Select Player Count:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(2, 3, 4).forEach { count ->
                                Button(
                                    onClick = { localPlayerCount = count },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (localPlayerCount == count) Color(0xFF00C853) else Color(0xFF1E3A52)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("$count Players", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Button(
                            onClick = { onStartLocalMatch(localPlayerCount) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            modifier = Modifier.fillMaxWidth().testTag("start_local_match_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("START LOCAL MATCH", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LobbyPlayerSlot(player: LudoPlayer?, slotNum: Int) {
    Card(
        modifier = Modifier.size(74.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (player != null) Color(0xFF0E304C) else Color(0xFF081826)
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (player != null) Color(0xFF00E676) else Color(0xFF263238)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (player != null) {
                Box(
                    modifier = Modifier.size(32.dp).background(Color(player.color.hexColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(player.name.firstOrNull()?.toString() ?: "P", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(player.name, color = Color.White, fontSize = 10.sp, maxLines = 1)
            } else {
                Text("Slot $slotNum", color = Color(0xFF546E7A), fontSize = 11.sp)
                Text("Empty", color = Color(0xFF37474F), fontSize = 9.sp)
            }
        }
    }
}
