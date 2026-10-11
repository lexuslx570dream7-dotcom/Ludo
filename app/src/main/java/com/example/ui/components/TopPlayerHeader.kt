package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.UserAccount
import com.example.ui.theme.*

@Composable
fun TopPlayerHeader(
    userAccount: UserAccount,
    onAvatarClick: () -> Unit,
    onAddCoinsClick: () -> Unit,
    onAddGemsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Outer emerald container header matching the screenshot
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp), ambientColor = Color(0xFF00E676), spotColor = Color(0xFF00E676))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF004D40).copy(alpha = 0.95f),
                        Color(0xFF00796B).copy(alpha = 0.90f),
                        Color(0xFF004D40).copy(alpha = 0.95f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00E676),
                        Color(0xFF1DE9B6),
                        Color(0xFF00E676)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Player Avatar with glowing green ring and level badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clickable { onAvatarClick() }
                    .testTag("avatar_button")
            ) {
                // Glowing circular ring
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF00E676), Color(0xFF00B0FF))
                            ),
                            shape = CircleShape
                        )
                        .border(2.5.dp, Color(0xFF00E676), CircleShape)
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B192C)),
                    contentAlignment = Alignment.Center
                ) {
                    // Stylized player icon/avatar
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E88E5), Color(0xFF0D47A1))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Player Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Level Badge (e.g. 12)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(Color(0xFF0A192F), CircleShape)
                        .border(1.5.dp, Color(0xFFFFD54F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${userAccount.level}",
                        color = Color(0xFFFFD54F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Coin Balance Pill
            CurrencyPill(
                amount = "${userAccount.coins}",
                isCoin = true,
                onAddClick = onAddCoinsClick,
                testTag = "add_coins_button"
            )

            // Gem Balance Pill
            CurrencyPill(
                amount = "${userAccount.gems}",
                isCoin = false,
                onAddClick = onAddGemsClick,
                testTag = "add_gems_button"
            )
        }
    }
}

@Composable
private fun CurrencyPill(
    amount: String,
    isCoin: Boolean,
    onAddClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF00332C), Color(0xFF001E1A))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(start = 6.dp, end = 3.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Icon (Coin or Gem)
        if (isCoin) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFEE58), Color(0xFFFF8F00))
                        ),
                        CircleShape
                    )
                    .border(1.dp, Color(0xFFFFF9C4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🪙",
                    fontSize = 13.sp
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF80D8FF), Color(0xFF0091EA))
                        ),
                        RoundedCornerShape(6.dp)
                    )
                    .border(1.dp, Color(0xFFE1F5FE), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💎",
                    fontSize = 13.sp
                )
            }
        }

        // Amount text
        Text(
            text = amount,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        // Yellow "+" button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable { onAddClick() }
                .testTag(testTag)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFEB3B), Color(0xFFFF9800))
                    ),
                    CircleShape
                )
                .border(1.2.dp, Color(0xFFFFF9C4), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Currency",
                tint = Color(0xFF3E2723),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
