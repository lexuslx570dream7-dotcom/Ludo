package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.GameBgDeep
import com.example.ui.theme.GameBgSurface

@Composable
fun VIPRoomScreen(
    userCoins: Long,
    onEnterVipMatch: (GameMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2A0812),
                        Color(0xFF150409),
                        GameBgDeep
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("👑 Royal VIP High Roller", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 20.sp)
        }

        // Luxury VIP Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFFFD54F))
                .clip(RoundedCornerShape(22.dp))
                .border(2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF1744))), RoundedCornerShape(22.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_card_vip),
                contentDescription = "VIP Room",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .padding(14.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ROYAL HIGH-ROLLER SALON", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("Stakes for Kings & Queens", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }

        // Stakes Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A12)),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("TABLE SPECIFICATIONS", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ENTRY STAKE", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                        Text("5,000 🪙", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("WINNER PRIZE", color = Color(0xFFB0BEC5), fontSize = 11.sp)
                        Text("19,000 🪙", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val hasEnoughCoins = userCoins >= 5000L

                Button(
                    onClick = { onEnterVipMatch(GameMode.VIP_ROOM) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasEnoughCoins) Color(0xFFFFB300) else Color(0xFF5D4037)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("enter_vip_table_btn")
                ) {
                    Text(
                        text = if (hasEnoughCoins) "ENTER VIP TABLE" else "GET COINS TO ENTER (5,000 NEEDED)",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // VIP Exclusive Privileges
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18080E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("👑 VIP Room Exclusive Privileges", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("• Double XP progression on all matches", color = Color.White, fontSize = 12.sp)
                Text("• Golden Dice custom sound effects", color = Color.White, fontSize = 12.sp)
                Text("• Exclusive VIP Crown badge on leaderboard", color = Color.White, fontSize = 12.sp)
                Text("• Access to high-stakes tournament invitations", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
