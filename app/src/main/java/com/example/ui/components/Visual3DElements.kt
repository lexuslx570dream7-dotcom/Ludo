package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerColor

/**
 * Premium tactile 3D Pushable Button with bevel highlights, extruded bottom depth, and rich game gradients.
 */
@Composable
fun Glossy3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(Color(0xFFFFD54F), Color(0xFFFF8F00), Color(0xFFE65100)),
    depthColor: Color = Color(0xFFBF360C),
    textColor: Color = Color.White,
    fontSize: Int = 16,
    icon: String? = null,
    testTag: String = "glossy_3d_button",
    enabled: Boolean = true
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
                indication = null,
                enabled = enabled
            ) { onClick() }
            .padding(top = topPadding)
    ) {
        // Bottom extruded 3D shadow depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = bottomDepth)
                .background(if (enabled) depthColor else Color(0xFF263238), RoundedCornerShape(16.dp))
        )

        // Main 3D Button Face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(if (enabled) 8.dp else 0.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (enabled) {
                        Brush.verticalGradient(gradientColors)
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFF546E7A), Color(0xFF37474F)))
                    }
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 12.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Specular top highlight glare
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(6.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Text(text = icon, fontSize = (fontSize + 4).sp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = textColor,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Polished glowing 3D "LUDO" Game Logo area with ambient glow, golden extruded bevel, and royal crown.
 * Meets Section 2: Simple, clean, professional logo area without noisy banner graphics.
 */
@Composable
fun Clean3DLudoLogo(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    val crownBounce by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crown_float"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Royal Crown
        Box(
            modifier = Modifier
                .offset(y = crownBounce.dp)
                .size(38.dp)
                .shadow(12.dp, CircleShape, ambientColor = Color(0xFFFFD54F), spotColor = Color(0xFFFFD54F))
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFFFF176), Color(0xFFFF8F00))
                    ),
                    CircleShape
                )
                .border(2.dp, Color(0xFFFFF9C4), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👑", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Glowing 3D "LUDO" Lettering
        Box(contentAlignment = Alignment.Center) {
            // Ambient Glow aura behind text
            Canvas(modifier = Modifier.size(width = 200.dp, height = 48.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFD54F).copy(alpha = 0.35f), Color.Transparent)
                    ),
                    radius = size.width / 2.5f
                )
            }

            // 3D Extruded Letters
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    'L' to Color(0xFFFF2A4B),
                    'U' to Color(0xFF00C853),
                    'D' to Color(0xFFFFD600),
                    'O' to Color(0xFF00B0FF)
                ).forEach { (char, color) ->
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 50.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = color)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        color.copy(alpha = 0.9f),
                                        color,
                                        Color(0xFF0B192C)
                                    )
                                )
                            )
                            .border(
                                2.dp,
                                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.8f), color)),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Top glare streak
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(4.dp)
                                .align(Alignment.TopCenter)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                        )
                        Text(
                            text = char.toString(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3D Glossy Pawn Token with realistic spherical head, tapered conical neck, and reflective rim.
 */
@Composable
fun GlossyPawn3D(
    color: PlayerColor,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = false,
    size: Dp = 32.dp
) {
    val baseColor = when (color) {
        PlayerColor.RED -> Color(0xFFFF1744)
        PlayerColor.GREEN -> Color(0xFF00E676)
        PlayerColor.YELLOW -> Color(0xFFFFEA00)
        PlayerColor.BLUE -> Color(0xFF00B0FF)
    }

    val deepColor = when (color) {
        PlayerColor.RED -> Color(0xFFB71C1C)
        PlayerColor.GREEN -> Color(0xFF1B5E20)
        PlayerColor.YELLOW -> Color(0xFFF57F17)
        PlayerColor.BLUE -> Color(0xFF0D47A1)
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (isSelectable) 10.dp else 4.dp,
                shape = CircleShape,
                spotColor = if (isSelectable) Color(0xFFFFD54F) else Color.Black
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // 1. Drop shadow base
            drawOval(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(w * 0.15f, h * 0.75f),
                size = Size(w * 0.7f, h * 0.25f)
            )

            // 2. Conical flared base
            val basePath = Path().apply {
                moveTo(w * 0.3f, h * 0.45f)
                lineTo(w * 0.7f, h * 0.45f)
                lineTo(w * 0.88f, h * 0.82f)
                quadraticBezierTo(w * 0.5f, h * 0.95f, w * 0.12f, h * 0.82f)
                close()
            }
            drawPath(
                path = basePath,
                brush = Brush.verticalGradient(
                    colors = listOf(baseColor, deepColor),
                    startY = h * 0.45f,
                    endY = h * 0.88f
                )
            )

            // 3. Spherical glossy head
            val headCenter = Offset(w * 0.5f, h * 0.32f)
            val headRadius = w * 0.28f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        baseColor,
                        deepColor
                    ),
                    center = Offset(headCenter.x - headRadius * 0.3f, headCenter.y - headRadius * 0.35f),
                    radius = headRadius * 1.2f
                ),
                radius = headRadius,
                center = headCenter
            )

            // 4. Specular glint on head
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = headRadius * 0.22f,
                center = Offset(headCenter.x - headRadius * 0.28f, headCenter.y - headRadius * 0.32f)
            )

            // 5. If selectable, draw glowing golden ring around base
            if (isSelectable) {
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = headRadius * 1.35f,
                    center = headCenter,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

/**
 * 3D Golden Coin Icon with rim, bevel, and sparkle
 */
@Composable
fun IconCoin3D(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape, ambientColor = Color(0xFFFFD54F), spotColor = Color(0xFFFF8F00)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.width / 2f
            val c = Offset(r, r)

            // Outer golden rim
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFFFEA00),
                        Color(0xFFFF8F00),
                        Color(0xFFFFD54F),
                        Color(0xFFFF6F00),
                        Color(0xFFFFEA00)
                    )
                ),
                radius = r,
                center = c
            )

            // Inner milled bevel
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFFFFF9C4), Color(0xFFFFB300), Color(0xFFE65100)),
                    center = Offset(r * 0.7f, r * 0.6f),
                    radius = r * 0.85f
                ),
                radius = r * 0.78f,
                center = c
            )

            // Specular glint
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = r * 0.2f,
                center = Offset(r * 0.6f, r * 0.55f)
            )
        }
    }
}

/**
 * 3D Emerald / Gem Icon with multi-faceted angular reflections
 */
@Composable
fun IconGem3D(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape, spotColor = Color(0xFF00E676)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Diamond silhouette
            val gemPath = Path().apply {
                moveTo(w * 0.25f, h * 0.2f)
                lineTo(w * 0.75f, h * 0.2f)
                lineTo(w * 0.95f, h * 0.45f)
                lineTo(w * 0.5f, h * 0.92f)
                lineTo(w * 0.05f, h * 0.45f)
                close()
            }

            drawPath(
                path = gemPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF69F0AE), Color(0xFF00C853), Color(0xFF004D40))
                )
            )

            // Upper facet highlight
            val topFacet = Path().apply {
                moveTo(w * 0.25f, h * 0.2f)
                lineTo(w * 0.75f, h * 0.2f)
                lineTo(w * 0.65f, h * 0.45f)
                lineTo(w * 0.35f, h * 0.45f)
                close()
            }
            drawPath(path = topFacet, color = Color(0xFFB9F6CA).copy(alpha = 0.65f))

            // Specular shine glint
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = w * 0.08f,
                center = Offset(w * 0.35f, h * 0.32f)
            )
        }
    }
}

/**
 * 3D Golden Trophy Icon with handles and victory luster
 */
@Composable
fun IconTrophy3D(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, CircleShape, spotColor = Color(0xFFFFD54F)),
        contentAlignment = Alignment.Center
    ) {
        Text("🏆", fontSize = (size.value * 0.8f).sp)
    }
}
