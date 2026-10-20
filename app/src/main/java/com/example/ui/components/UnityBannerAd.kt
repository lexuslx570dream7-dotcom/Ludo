package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.UnityAdsManager
import com.unity3d.services.banners.BannerView

/**
 * Production-ready Compose wrapper for Unity Ads Banner (BP_Banner_Android).
 * Automatically initializes, loads, manages the BannerView lifecycle,
 * and handles graceful fallbacks and reload transitions.
 */
@Composable
fun UnityBannerAd(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    var isAdLoaded by remember { mutableStateOf(false) }
    var bannerViewInstance by remember { mutableStateOf<BannerView?>(null) }

    // Initialize ads and trigger banner load
    LaunchedEffect(activity) {
        UnityAdsManager.initialize(activity) {
            bannerViewInstance?.load()
        }
    }

    DisposableEffect(activity) {
        onDispose {
            bannerViewInstance?.destroy()
            bannerViewInstance = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF040F1D),
                        Color(0xFF081C30),
                        Color(0xFF040F1D)
                    )
                )
            )
            .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(12.dp))
            .testTag("unity_banner_ad_container"),
        contentAlignment = Alignment.Center
    ) {
        // Native Unity Banner View
        AndroidView(
            factory = { ctx ->
                val bv = UnityAdsManager.createBannerView(
                    activity = activity,
                    widthDp = 320,
                    heightDp = 50,
                    onBannerLoaded = {
                        isAdLoaded = true
                    },
                    onBannerFailed = { error ->
                        isAdLoaded = false
                    }
                )
                bannerViewInstance = bv
                if (UnityAdsManager.isInitialized) {
                    bv.load()
                }
                bv
            },
            modifier = Modifier.wrapContentSize(),
            update = { bv ->
                // Banner view handles its own internal lifecycle
            }
        )

        // Subtle sponsor indicator badge if ad is preparing or loaded
        AnimatedVisibility(
            visible = !isAdLoaded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AD",
                        color = Color(0xFF00E5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Unity Live Ad Network • ${UnityAdsManager.BANNER_PLACEMENT_ID}",
                    color = Color(0xFF8197A4),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
