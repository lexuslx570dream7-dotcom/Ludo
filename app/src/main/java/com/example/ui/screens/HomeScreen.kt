package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.components.Clean3DLudoLogo
import com.example.ui.components.Glossy3DButton
import com.example.ui.components.TopPlayerHeader
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    userAccount: UserAccount,
    onAvatarClick: () -> Unit,
    onAddCoinsClick: () -> Unit,
    onAddGemsClick: () -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onOpenComputerMode: () -> Unit,
    onOpenPrivateRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlayerCountModal by remember { mutableStateOf(false) }

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
        // Top Player Header (Profile Avatar on the top-left, Coins, Gems with + buttons)
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
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 2: Clean, professional 3D glowing "LUDO" logo area
            Clean3DLudoLogo()

            // Section 6: Exactly 4 Game Mode Cards in 2x2 Grid:
            // 1. "2&4 Players"
            // 2. "Team"
            // 3. "Computer"
            // 4. "Private"

            // Row 1: "2&4 Players" & "Team"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: 2&4 Players
                Mode3DCard(
                    title = "2&4 Players",
                    subtitle = "Online Matchmaking",
                    imageRes = R.drawable.img_card_players_3d,
                    borderColor = Color(0xFFFF9100),
                    depthColor = Color(0xFFBF360C),
                    gradientStart = CardPlayersGradientStart,
                    gradientEnd = CardPlayersGradientEnd,
                    modifier = Modifier.weight(1f),
                    testTag = "card_2_and_4_players",
                    onClick = { showPlayerCountModal = true }
                )

                // Card 2: Team (2v2)
                Mode3DCard(
                    title = "Team",
                    subtitle = "2v2 Team Battle",
                    imageRes = R.drawable.img_card_team_3d,
                    borderColor = Color(0xFF00B0FF),
                    depthColor = Color(0xFF0D47A1),
                    gradientStart = CardTeamGradientStart,
                    gradientEnd = CardTeamGradientEnd,
                    modifier = Modifier.weight(1f),
                    testTag = "card_team",
                    onClick = { onSelectMode(GameMode.TEAM_2V2) }
                )
            }

            // Row 2: "Computer" & "Private"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Computer (replaces Tournament, instant offline/local bot game)
                Mode3DCard(
                    title = "Computer",
                    subtitle = "Instant vs AI",
                    imageRes = R.drawable.img_card_computer_3d,
                    borderColor = Color(0xFF00E676),
                    depthColor = Color(0xFF1B5E20),
                    gradientStart = Color(0xFF00C853),
                    gradientEnd = Color(0xFF004D40),
                    modifier = Modifier.weight(1f),
                    testTag = "card_computer",
                    onClick = onOpenComputerMode
                )

                // Card 4: Private (replaces VIP/Private with unified Private Room)
                Mode3DCard(
                    title = "Private",
                    subtitle = "Room with Friends",
                    imageRes = R.drawable.img_card_private_3d,
                    borderColor = Color(0xFFFFD54F),
                    depthColor = Color(0xFFE65100),
                    gradientStart = CardPrivateGradientStart,
                    gradientEnd = CardPrivateGradientEnd,
                    modifier = Modifier.weight(1f),
                    testTag = "card_private",
                    onClick = onOpenPrivateRoom
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Unity Ads Banner (Placement: BP_Banner_Android)
            com.example.ui.components.UnityBannerAd(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    // Modal when user taps "2&4 Players" - uses Polished 3D Button Style
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

/**
 * Premium 3D Game Mode Card with tactile depth, gloss frame, and vivid image artwork
 */
@Composable
private fun Mode3DCard(
    title: String,
    subtitle: String,
    imageRes: Int,
    borderColor: Color,
    depthColor: Color,
    gradientStart: Color,
    gradientEnd: Color,
    modifier: Modifier = Modifier,
    testTag: String = "mode_card",
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topPadding = if (isPressed) 4.dp else 0.dp
    val bottomDepth = if (isPressed) 2.dp else 6.dp

    Box(
        modifier = modifier
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(top = topPadding)
    ) {
        // 3D Bottom shadow slab
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = bottomDepth)
                .background(depthColor, RoundedCornerShape(20.dp))
        )

        // Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = borderColor)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF081C2D))
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.7f), borderColor)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            // Artwork Image
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark gradient overlay for typography readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color(0xFF030D16).copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            // Top specular highlight line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(4.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.4f))
            )

            // Title & Subtitle Badge at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 2 Players vs 4 Players Selection Modal using the polished 3D Button style
 */
@Composable
private fun PlayerCountSelectionModal(
    onSelect2Players: () -> Unit,
    onSelect4Players: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SELECT MATCH FORMAT",
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Real-time Firebase online matchmaking",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 3D Button for 2 Players
                Glossy3DButton(
                    text = "2 PLAYERS (1v1) - 500 🪙",
                    icon = "⚔️",
                    onClick = onSelect2Players,
                    gradientColors = listOf(Color(0xFFFF9100), Color(0xFFFF6D00), Color(0xFFE65100)),
                    depthColor = Color(0xFFBF360C),
                    fontSize = 14,
                    testTag = "select_2_players_btn"
                )

                // 3D Button for 4 Players
                Glossy3DButton(
                    text = "4 PLAYERS (FREE-FOR-ALL) - 1,000 🪙",
                    icon = "👑",
                    onClick = onSelect4Players,
                    gradientColors = listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF00796B)),
                    depthColor = Color(0xFF004D40),
                    fontSize = 14,
                    testTag = "select_4_players_btn"
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_modal_btn")
            ) {
                Text("CANCEL", color = Color(0xFF90A4AE), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF0A2238),
        shape = RoundedCornerShape(24.dp)
    )
}
