package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.viewmodel.StoreType

@Composable
fun CoinGemDialog(
    storeType: StoreType,
    onClaimFreeReward: () -> Unit,
    onPurchasePack: (amount: Long, isCoin: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    if (storeType == StoreType.NONE) return

    val isCoinStore = storeType == StoreType.COINS

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, if (isCoinStore) Color(0xFFFFD54F) else Color(0xFF00E5FF), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF081C2C))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCoinStore) "🪙 Coin Store & Rewards" else "💎 Diamond Gem Vault",
                        color = if (isCoinStore) Color(0xFFFFD54F) else Color(0xFF00E5FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_store_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Free Daily Gift Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClaimFreeReward() }
                        .testTag("claim_free_reward_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "🎁 Daily Free Reward",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isCoinStore) "+500 Free Coins" else "+2 Free Gems",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onClaimFreeReward,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("CLAIM", color = Color(0xFF003300), fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Special Offers",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isCoinStore) {
                    StoreBundleRow("Handful of Coins", "1,000 Coins", "🪙", 1000L, true, onPurchasePack)
                    Spacer(modifier = Modifier.height(8.dp))
                    StoreBundleRow("Sack of Gold", "5,000 Coins", "💰", 5000L, true, onPurchasePack)
                    Spacer(modifier = Modifier.height(8.dp))
                    StoreBundleRow("Royal Chest", "20,000 Coins", "👑", 20000L, true, onPurchasePack)
                } else {
                    StoreBundleRow("Pouch of Diamonds", "10 Gems", "💎", 10L, false, onPurchasePack)
                    Spacer(modifier = Modifier.height(8.dp))
                    StoreBundleRow("Jeweled Casket", "50 Gems", "✨", 50L, false, onPurchasePack)
                    Spacer(modifier = Modifier.height(8.dp))
                    StoreBundleRow("Emperor's Vault", "200 Gems", "🌟", 200L, false, onPurchasePack)
                }
            }
        }
    }
}

@Composable
private fun StoreBundleRow(
    title: String,
    subtitle: String,
    icon: String,
    amount: Long,
    isCoin: Boolean,
    onPurchase: (Long, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D283D), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E496B), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF051724), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
            Column {
                Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = subtitle, color = if (isCoin) Color(0xFFFFD54F) else Color(0xFF80D8FF), fontSize = 11.sp)
            }
        }

        Button(
            onClick = { onPurchase(amount, isCoin) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCoin) Color(0xFFFFB300) else Color(0xFF00B0FF)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("buy_pack_${amount}")
        ) {
            Text("GET", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
