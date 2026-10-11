package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GameMode
import com.example.model.UserAccount
import com.example.ui.components.TopPlayerHeader
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    userAccount: UserAccount,
    onAvatarClick: () -> Unit,
    onAddCoinsClick: () -> Unit,
    onAddGemsClick: () -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onOpenTournament: () -> Unit,
    onOpenPrivateLocal: () -> Unit,
    onOpenVipRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlayerCountModal by remember { mutableStateOf(false) }

    // Subtle breathing animation for hero elements
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")
    val heroDiceScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dice_float"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GameBgDeep,
                        GameBgTop,
                        GameBgSurface,
                        Color(0xFF040E18)
                    )
                )
            )
    ) {
        // Top Player Header (Avatar, Coins, Gems with + buttons)
        TopPlayerHeader(
            userAccount = userAccount,
            onAvatarClick = onAvatarClick,
            onAddCoinsClick = onAddCoinsClick,
            onAddGemsClick = onAddGemsClick
        )

        // Scrollable Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Ludo Hero Section with 3D board, dice, crown, pawns
            HeroLudoSection(
                scale = heroDiceScale,
                onHeroClick = { showPlayerCountModal = true }
            )

            // Game Mode Cards Grid
            // Row 1: "2&4 Players" (Orange) and "Team" (Purple)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 2&4 Players Card
                LargeModeCard(
                    title = "2&4 Players",
                    imageRes = R.drawable.img_card_players,
                    gradientStart = CardPlayersGradientStart,
                    gradientEnd = CardPlayersGradientEnd,
                    modifier = Modifier.weight(1f),
                    testTag = "card_2_and_4_players",
                    onClick = { showPlayerCountModal = true }
                )

                // Team Card
                LargeModeCard(
                    title = "Team",
                    imageRes = R.drawable.img_card_team,
                    gradientStart = CardTeamGradientStart,
                    gradientEnd = CardTeamGradientEnd,
                    modifier = Modifier.weight(1f),
                    testTag = "card_team",
                    onClick = { onSelectMode(GameMode.TEAM_2V2) }
                )
            }

            // Row 2: "Tournament", "Private&Local", "VIP Room"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tournament Card
                SmallModeCard(
                    title = "Tournament",
                    customVisual = {
                        TournamentTrophyVisual()
                    },
                    gradientStart = CardTournamentGradientStart,
                    gradientEnd = CardTournamentGradientEnd,
                    modifier = Modifier.weight(1f),
                    testTag = "card_tournament",
                    onClick = onOpenTournament
                )

                // Private & Local Card
                SmallModeCard(
                    title = "Private&Local",
                    customVisual = {
                        PrivateLocalVisual()
                    },
                    gradientStart = CardPrivateGradientStart,
                    gradientEnd = CardPrivateGradientEnd,
                    modifier = Modifier.weight(1f),
                    testTag = "card_private_local",
                    onClick = onOpenPrivateLocal
                )

                // VIP Room Card
                SmallModeCard(
                    title = "VIP Room",
                    imageRes = R.drawable.img_card_vip,
                    gradientStart = CardVipGradientStart,
                    gradientEnd = CardVipGradientEnd,
                    modifier = Modifier.weight(1f),
                    testTag = "card_vip_room",
                    onClick = onOpenVipRoom
                )
            }

            // Bottom 3D board close-up banner matching reference screenshot
            BottomLudoBoardBanner()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal when user taps "2&4 Players"
    if (showPlayerCountModal) {
        PlayerCountSelectionModal(
            onSelect2Players = {
                showPlayerCountModal = false
                onSelectMode(GameMode.TWO_PLAYERS)
            },
            onSelect4Players = {
                showPlayerCountModal = false
                onSelectMode(GameMode.FOUR_PLAYERS)
            },
            onDismiss = { showPlayerCountModal = false }
        )
    }
}

@Composable
private fun HeroLudoSection(
    scale: Float,
    onHeroClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(16.dp, RoundedCornerShape(22.dp), ambientColor = Color(0xFF00E676))
            .clip(RoundedCornerShape(22.dp))
            .border(2.dp, Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFF00E676))), RoundedCornerShape(22.dp))
            .clickable { onHeroClick() }
            .testTag("hero_ludo_banner"),
        contentAlignment = Alignment.Center
    ) {
        // Hero Image Background
        Image(
            painter = painterResource(id = R.drawable.img_ludo_hero),
            contentDescription = "Ludo Board Hero",
            modifier = Modifier
                .fillMaxSize()
                .scale(scale),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay for readability and 3D depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        // 3D "LUDO" Title with Royal Crown on top
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Crown badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFEE58), Color(0xFFFF8F00))
                        ),
                        CircleShape
                    )
                    .border(1.5.dp, Color(0xFFFFF9C4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👑", fontSize = 20.sp)
            }

            // Big 3D "LUDO" typography
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LudoLetterTile("L", LudoRed)
                LudoLetterTile("U", LudoYellow)
                LudoLetterTile("D", LudoGreen)
                LudoLetterTile("O", LudoBlue)
            }
        }
    }
}

@Composable
private fun LudoLetterTile(letter: String, color: Color) {
    Box(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(8.dp), spotColor = Color.Black)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.copy(alpha = 0.8f))
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(2.dp, Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun LargeModeCard(
    title: String,
    imageRes: Int,
    gradientStart: Color,
    gradientEnd: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(175.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = gradientStart)
            .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = gradientEnd)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Glossy title banner at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SmallModeCard(
    title: String,
    imageRes: Int? = null,
    customVisual: (@Composable () -> Unit)? = null,
    gradientStart: Color,
    gradientEnd: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(138.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = gradientStart)
            .border(2.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = gradientEnd)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageRes != null) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (customVisual != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(gradientStart, gradientEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    customVisual()
                }
            }

            // Title at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(vertical = 6.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TournamentTrophyVisual() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFFFEE58), Color(0xFFFF8F00))
                    ),
                    CircleShape
                )
                .border(2.dp, Color(0xFFFFF9C4), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🏆", fontSize = 30.sp)
        }
    }
}

@Composable
private fun PrivateLocalVisual() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF80D8FF), Color(0xFF0091EA))
                    ),
                    RoundedCornerShape(12.dp)
                )
                .border(2.dp, Color(0xFFE1F5FE), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📱", fontSize = 28.sp)
        }
    }
}

@Composable
private fun BottomLudoBoardBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .border(1.5.dp, Color(0xFF00E676).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .background(Color(0xFF071927))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_ludo_hero),
            contentDescription = "Ludo Board Close-up",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF06111C).copy(alpha = 0.8f))
                    )
                )
        )
    }
}

@Composable
private fun PlayerCountSelectionModal(
    onSelect2Players: () -> Unit,
    onSelect4Players: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select Players Mode",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Choose an online matchmaking format:",
                    color = Color(0xFFB0BEC5),
                    fontSize = 13.sp
                )

                // 2 Players Option
                Button(
                    onClick = onSelect2Players,
                    modifier = Modifier.fillMaxWidth().testTag("select_2_players_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("2 Players (1v1)", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Entry: 500 🪙", fontSize = 12.sp, color = Color(0xFFFFF9C4))
                    }
                }

                // 4 Players Option
                Button(
                    onClick = onSelect4Players,
                    modifier = Modifier.fillMaxWidth().testTag("select_4_players_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("4 Players Free-for-All", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Entry: 1,000 🪙", fontSize = 12.sp, color = Color(0xFFFFF9C4))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF90A4AE))
            }
        },
        containerColor = Color(0xFF0C2438),
        shape = RoundedCornerShape(20.dp)
    )
}
