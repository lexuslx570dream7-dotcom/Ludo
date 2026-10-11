package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Achievement
import com.example.model.UserAccount
import com.example.ui.theme.GameBgDeep
import com.example.ui.theme.GameBgSurface

@Composable
fun ProfileScreen(
    userAccount: UserAccount,
    achievements: List<Achievement>,
    onToggleMusic: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onUpdateUsername: (String) -> Unit,
    onAddCoinsClick: () -> Unit,
    onAddGemsClick: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEditNameDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GameBgDeep, GameBgSurface, Color(0xFF030E19))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Large Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(22.dp))
                .border(2.dp, Brush.horizontalGradient(listOf(Color(0xFF00E676), Color(0xFFFFD54F))), RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2238)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Avatar with golden border
                Box(modifier = Modifier.size(80.dp)) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFF00E676))),
                                CircleShape
                            )
                            .border(3.dp, Color(0xFF00E676), CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E88E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    // Level Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(26.dp)
                            .background(Color(0xFF0A192F), CircleShape)
                            .border(2.dp, Color(0xFFFFD54F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${userAccount.level}",
                            color = Color(0xFFFFD54F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = userAccount.username,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = { showEditNameDialog = true },
                        modifier = Modifier.size(24.dp).testTag("edit_username_btn")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    text = "Player ID: ${userAccount.userId}",
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // XP Progress Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Level Progress", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                        Text("${userAccount.xp} / ${userAccount.maxXp} XP", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { userAccount.xp.toFloat() / userAccount.maxXp.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Color(0xFF00E676),
                        trackColor = Color(0xFF1E3A52)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Currencies display with + buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onAddCoinsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00382E)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
                    ) {
                        Text("🪙 ${userAccount.coins} +", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onAddGemsClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00382E)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Text("💎 ${userAccount.gems} +", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Stats Grid
        Text(
            text = "📊 Career Statistics",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(title = "Matches", value = "${userAccount.totalGames}", modifier = Modifier.weight(1f))
            StatCard(title = "Wins", value = "${userAccount.wins}", modifier = Modifier.weight(1f))
            StatCard(title = "Win Rate", value = "${userAccount.winRate.toInt()}%", modifier = Modifier.weight(1f))
            StatCard(title = "Streak", value = "${userAccount.winStreak}🔥", modifier = Modifier.weight(1f))
        }

        // Achievements Section
        Text(
            text = "🏆 Achievements",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            achievements.forEach { achievement ->
                AchievementRow(achievement = achievement)
            }
        }

        // Settings Section
        Text(
            text = "⚙️ Game Settings",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2238)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Music Toggle
                SettingSwitchRow(
                    title = "Ludo Background Music",
                    icon = Icons.Default.MusicNote,
                    isChecked = userAccount.musicEnabled,
                    onCheckedChange = onToggleMusic,
                    testTag = "music_toggle"
                )

                HorizontalDivider(color = Color(0xFF1E3A52))

                // Sound FX Toggle
                SettingSwitchRow(
                    title = "Game Sound Effects",
                    icon = Icons.Default.VolumeUp,
                    isChecked = userAccount.soundEnabled,
                    onCheckedChange = onToggleSound,
                    testTag = "sound_toggle"
                )

                HorizontalDivider(color = Color(0xFF1E3A52))

                // Vibration Toggle
                SettingSwitchRow(
                    title = "Vibration Feedback",
                    icon = Icons.Default.Vibration,
                    isChecked = userAccount.vibrationEnabled,
                    onCheckedChange = onToggleVibration,
                    testTag = "vibration_toggle"
                )

                HorizontalDivider(color = Color(0xFF1E3A52))

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sign_out_button")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out of Google", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showEditNameDialog) {
        var newName by remember { mutableStateOf(userAccount.username) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Change Username", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedBorderColor = Color(0xFF455A64),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onUpdateUsername(newName.trim())
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = Color(0xFF90A4AE))
                }
            },
            containerColor = Color(0xFF0D253B)
        )
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B253D)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text(title, color = Color(0xFF90A4AE), fontSize = 10.sp)
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) Color(0xFF0D2B44) else Color(0xFF081826)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (achievement.isUnlocked) Color(0xFF00E676).copy(alpha = 0.5f) else Color(0xFF1E3A52)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF041320), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(achievement.iconEmoji, fontSize = 20.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    color = if (achievement.isUnlocked) Color.White else Color(0xFF78909C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = achievement.description,
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp
                )
                if (!achievement.isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { achievement.currentProgress.toFloat() / achievement.maxProgress.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = Color(0xFFFFD54F),
                        trackColor = Color(0xFF1E3A52)
                    )
                }
            }

            if (achievement.isUnlocked) {
                Text("✓ Done", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            } else {
                Text("${achievement.currentProgress}/${achievement.maxProgress}", color = Color(0xFF78909C), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = title, tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00C853),
                uncheckedThumbColor = Color(0xFF78909C),
                uncheckedTrackColor = Color(0xFF1E3A52)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
