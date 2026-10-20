package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.UnityAdsManager
import com.example.model.UserAccount
import com.example.ui.components.Glossy3DButton
import com.example.ui.components.IconCoin3D
import com.example.ui.components.IconGem3D
import com.example.ui.components.UnityBannerAd
import com.example.ui.theme.GameBgDeep
import com.example.ui.theme.GameBgSurface
import kotlinx.coroutines.launch

@Composable
fun RewardsScreen(
    userAccount: UserAccount,
    onCreditCoins: (amount: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isWatchingAd by remember { mutableStateOf(false) }
    var dailyClaimed by remember { mutableStateOf(false) }
    var chestClaimed by remember { mutableStateOf(false) }

    // Breathing pulse for highlighted cards
    val infiniteTransition = rememberInfiniteTransition(label = "reward_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(GameBgDeep, GameBgSurface, Color(0xFF020B14))
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Rewards Center Title & Current Balances
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎁 REWARDS CENTER",
                    color = Color(0xFFFFD54F),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Watch ads & complete daily quests to earn free coins",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
            }

            // Real-Time Coin & Gem Status Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD54F))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00382E), Color(0xFF005646), Color(0xFF00382E))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconCoin3D(size = 32.dp)
                    Column {
                        Text(
                            text = "${userAccount.coins}",
                            color = Color(0xFFFFEE58),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(text = "Total Coins", color = Color(0xFF80CBC4), fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFF00E676).copy(alpha = 0.4f))
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconGem3D(size = 32.dp)
                    Column {
                        Text(
                            text = "${userAccount.gems}",
                            color = Color(0xFF69F0AE),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(text = "Total Gems", color = Color(0xFF80CBC4), fontSize = 11.sp)
                    }
                }
            }

            // Unity Ads Banner (Placement: BP_Banner_Android)
            UnityBannerAd(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )

            // Card 1: Featured Rewarded Video (Unity Ads)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
                    .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFFFF9100), spotColor = Color(0xFFFF9100))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1B2838), Color(0xFF0E1A26))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        2.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFFFFD54F), Color(0xFFFF6D00), Color(0xFFFFD54F))
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tag Badge
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFFFF3D00), Color(0xFFFF9100))),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡ INSTANT REWARD (UNITY ADS)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Video Reel & Coins Visual
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color(0xFFFFD54F))
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFFD54F), Color(0xFFFF6D00))
                                    ),
                                    CircleShape
                                )
                                .border(2.dp, Color(0xFFFFF9C4), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎬", fontSize = 34.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Watch Rewarded Video",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "Get +500 Coins immediately",
                                color = Color(0xFFFFD54F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Watch Ad Button
                    Glossy3DButton(
                        text = if (isWatchingAd) "PREPARING AD..." else "WATCH VIDEO AD (+500 🪙)",
                        icon = if (isWatchingAd) "⏳" else "▶",
                        enabled = !isWatchingAd,
                        onClick = {
                            if (activity == null) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Unable to initialize ad: Activity context missing")
                                }
                                return@Glossy3DButton
                            }
                            isWatchingAd = true
                            UnityAdsManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { coins ->
                                    isWatchingAd = false
                                    onCreditCoins(coins)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("🎉 Awesome! +$coins Coins added to your account!")
                                    }
                                },
                                onAdFailed = { errorMsg ->
                                    isWatchingAd = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Ad notice: $errorMsg")
                                    }
                                }
                            )
                        },
                        gradientColors = listOf(Color(0xFFFFB300), Color(0xFFFF6F00), Color(0xFFD84315)),
                        depthColor = Color(0xFFBF360C),
                        testTag = "watch_rewarded_ad_btn"
                    )
                }
            }

            // Card 2: Daily Login Streak
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F2236), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF004D40), CircleShape)
                                .border(1.5.dp, Color(0xFF00E676), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📅", fontSize = 24.sp)
                        }

                        Column {
                            Text(
                                text = "Daily Login Bonus",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "+1,000 Free Coins",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Glossy3DButton(
                        text = if (dailyClaimed) "CLAIMED" else "CLAIM",
                        enabled = !dailyClaimed,
                        gradientColors = listOf(Color(0xFF00E676), Color(0xFF00C853), Color(0xFF00897B)),
                        depthColor = Color(0xFF004D40),
                        fontSize = 13,
                        modifier = Modifier.width(110.dp),
                        onClick = {
                            dailyClaimed = true
                            onCreditCoins(1000L)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("🎉 Claimed 1,000 Daily Streak Coins!")
                            }
                        },
                        testTag = "claim_daily_btn"
                    )
                }
            }

            // Card 3: Hourly Mystery Chest
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F2236), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFF00B0FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFF0D47A1), CircleShape)
                                .border(1.5.dp, Color(0xFF00B0FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧰", fontSize = 24.sp)
                        }

                        Column {
                            Text(
                                text = "Hourly Mystery Box",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "+250 Free Coins",
                                color = Color(0xFF40C4FF),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Glossy3DButton(
                        text = if (chestClaimed) "OPENED" else "OPEN",
                        enabled = !chestClaimed,
                        gradientColors = listOf(Color(0xFF40C4FF), Color(0xFF0091EA), Color(0xFF01579B)),
                        depthColor = Color(0xFF0D47A1),
                        fontSize = 13,
                        modifier = Modifier.width(110.dp),
                        onClick = {
                            chestClaimed = true
                            onCreditCoins(250L)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("✨ You opened the chest and found 250 Coins!")
                            }
                        },
                        testTag = "open_mystery_chest_btn"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
