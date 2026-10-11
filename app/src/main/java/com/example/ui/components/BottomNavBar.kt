package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AppScreen

@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color(0xFF00E676), spotColor = Color(0xFF00E676))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A2438).copy(alpha = 0.95f),
                        Color(0xFF04121E).copy(alpha = 0.98f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF00B0FF).copy(alpha = 0.3f),
                        Color(0xFF00E676).copy(alpha = 0.5f),
                        Color(0xFF00B0FF).copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Tab
            NavTabItem(
                title = "Home",
                isSelected = currentScreen == AppScreen.HOME,
                testTag = "nav_home",
                icon = { isSelected ->
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF00E676), Color(0xFF00C853))
                                    ),
                                    CircleShape
                                )
                                .border(1.5.dp, Color(0xFFB9F6CA), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = Color(0xFF80CBC4),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                onClick = { onTabSelected(AppScreen.HOME) }
            )

            // Social Tab
            NavTabItem(
                title = "Social",
                isSelected = currentScreen == AppScreen.SOCIAL,
                testTag = "nav_social",
                icon = { isSelected ->
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF00E676), Color(0xFF00C853))
                                    ),
                                    CircleShape
                                )
                                .border(1.5.dp, Color(0xFFB9F6CA), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Social",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Social",
                            tint = Color(0xFF80CBC4),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                onClick = { onTabSelected(AppScreen.SOCIAL) }
            )

            // Profile Tab
            NavTabItem(
                title = "Profile",
                isSelected = currentScreen == AppScreen.PROFILE,
                testTag = "nav_profile",
                icon = { isSelected ->
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF00E676), Color(0xFF00C853))
                                    ),
                                    CircleShape
                                )
                                .border(1.5.dp, Color(0xFFB9F6CA), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF80CBC4),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                },
                onClick = { onTabSelected(AppScreen.PROFILE) }
            )
        }
    }
}

@Composable
private fun NavTabItem(
    title: String,
    isSelected: Boolean,
    testTag: String,
    icon: @Composable (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(isSelected)

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = title,
            color = if (isSelected) Color(0xFF00E676) else Color(0xFF90A4AE),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )

        // Green indicator underline when selected
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(3.dp)
                    .background(Color(0xFF00E676), RoundedCornerShape(2.dp))
            )
        } else {
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}
