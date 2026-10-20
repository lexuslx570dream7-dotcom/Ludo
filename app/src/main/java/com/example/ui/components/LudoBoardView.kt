package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun LudoBoardView(
    gameState: LudoGameState,
    onPawnClick: (pawnId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulsing animation for movable pawns
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pawn_pulse"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(20.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF00E676))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF4E342E), Color(0xFF3E2723))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(4.dp, Brush.linearGradient(listOf(Color(0xFFFFD54F), Color(0xFF8D6E63))), RoundedCornerShape(18.dp))
            .padding(6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFFFDE7))
    ) {
        // Draw the authentic 15x15 Ludo Grid Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val boardSize = size.width
            val cellSize = boardSize / 15f

            drawLudoBaseAndTracks(cellSize)
        }

        // Overlay Pawns and Interactive elements
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val boardW = maxWidth
            val cellDp = boardW / 15f

            // Render all player pawns
            gameState.players.forEach { player ->
                val isCurrentPlayer = gameState.currentPlayer?.id == player.id

                player.pawns.forEach { pawn ->
                    val isSelectable = isCurrentPlayer && gameState.selectablePawnIds.contains(pawn.id)

                    val gridCoord = calculatePawnGridCoordinate(player.color, pawn)

                    if (gridCoord != null) {
                        val pawnOffsetX = cellDp * gridCoord.first
                        val pawnOffsetY = cellDp * gridCoord.second

                        Box(
                            modifier = Modifier
                                .offset(x = pawnOffsetX, y = pawnOffsetY)
                                .size(cellDp)
                                .scale(if (isSelectable) pulseScale else 1f)
                                .clickable(enabled = isSelectable) {
                                    onPawnClick(pawn.id)
                                }
                                .testTag("pawn_${player.color.name}_${pawn.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Pawn3DPiece(
                                color = player.color,
                                isSelectable = isSelectable,
                                size = cellDp * 0.85f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Pawn3DPiece(
    color: PlayerColor,
    isSelectable: Boolean,
    size: androidx.compose.ui.unit.Dp
) {
    GlossyPawn3D(
        color = color,
        isSelectable = isSelectable,
        size = size
    )
}


// 15x15 standard Ludo track grid coordinate mapper
fun calculatePawnGridCoordinate(color: PlayerColor, pawn: Pawn): Pair<Float, Float>? {
    if (pawn.isFinished) {
        // Center Home
        return Pair(7f, 7f)
    }

    if (pawn.isInBase) {
        // In the 4 home base quadrants matching screenshot (Yellow top-left, Blue top-right, Green bottom-left, Red bottom-right)
        val baseCoords = when (color) {
            PlayerColor.YELLOW -> listOf(Pair(1.8f, 1.8f), Pair(3.8f, 1.8f), Pair(1.8f, 3.8f), Pair(3.8f, 3.8f))
            PlayerColor.BLUE -> listOf(Pair(10.8f, 1.8f), Pair(12.8f, 1.8f), Pair(10.8f, 3.8f), Pair(12.8f, 3.8f))
            PlayerColor.GREEN -> listOf(Pair(1.8f, 10.8f), Pair(3.8f, 10.8f), Pair(1.8f, 12.8f), Pair(3.8f, 12.8f))
            PlayerColor.RED -> listOf(Pair(10.8f, 10.8f), Pair(12.8f, 10.8f), Pair(10.8f, 12.8f), Pair(12.8f, 12.8f))
        }
        return baseCoords.getOrNull(pawn.id.coerceIn(0, 3))
    }

    // Home Runway (steps 51..55)
    if (pawn.isInHomePath) {
        val dist = pawn.step - 50 // 1..5
        return when (color) {
            PlayerColor.YELLOW -> Pair(dist.toFloat(), 7f)
            PlayerColor.BLUE -> Pair(7f, dist.toFloat())
            PlayerColor.RED -> Pair((14 - dist).toFloat(), 7f)
            PlayerColor.GREEN -> Pair(7f, (14 - dist).toFloat())
        }
    }

    // Standard 52-tile circuit coordinates
    val circuitIndex = LudoLogic.getBoardCircuitIndex(color, pawn.step) ?: return null
    return TRACK_COORDINATES.getOrNull(circuitIndex)
}

// Precalculated standard 52 outer track grid tiles (0..51)
val TRACK_COORDINATES: List<Pair<Float, Float>> = listOf(
    // Circuit start
    Pair(1f, 6f), Pair(2f, 6f), Pair(3f, 6f), Pair(4f, 6f), Pair(5f, 6f), // 0..4
    Pair(6f, 5f), Pair(6f, 4f), Pair(6f, 3f), Pair(6f, 2f), Pair(6f, 1f), Pair(6f, 0f), // 5..10
    Pair(7f, 0f), // 11
    Pair(8f, 0f), Pair(8f, 1f), Pair(8f, 2f), Pair(8f, 3f), Pair(8f, 4f), Pair(8f, 5f), // 12..17
    Pair(9f, 6f), Pair(10f, 6f), Pair(11f, 6f), Pair(12f, 6f), Pair(13f, 6f), Pair(14f, 6f), // 18..23
    Pair(14f, 7f), // 24
    Pair(14f, 8f), Pair(13f, 8f), Pair(12f, 8f), Pair(11f, 8f), Pair(10f, 8f), Pair(9f, 8f), // 25..30
    Pair(8f, 9f), Pair(8f, 10f), Pair(8f, 11f), Pair(8f, 12f), Pair(8f, 13f), Pair(8f, 14f), // 31..36
    Pair(7f, 14f), // 37
    Pair(6f, 14f), Pair(6f, 13f), Pair(6f, 12f), Pair(6f, 11f), Pair(6f, 10f), Pair(6f, 9f), // 38..43
    Pair(5f, 8f), Pair(4f, 8f), Pair(3f, 8f), Pair(2f, 8f), Pair(1f, 8f), Pair(0f, 8f), // 44..49
    Pair(0f, 7f), Pair(0f, 6f) // 50..51
)

fun DrawScope.drawLudoBaseAndTracks(cellSize: Float) {
    val yellowColor = Color(0xFFE5A100)
    val blueColor = Color(0xFF1E88E5)
    val greenColor = Color(0xFF2E7D32)
    val redColor = Color(0xFFE53935)
    val borderCol = Color(0xFF455A64)

    // 1. Draw 4 Home Base Quadrants matching screenshot
    // Yellow Base (Top Left)
    drawRect(yellowColor, Offset(0f, 0f), Size(cellSize * 6, cellSize * 6))
    drawCircle(Color(0xFFFFF9C4), cellSize * 2.3f, Offset(cellSize * 3f, cellSize * 3f))
    drawCircle(yellowColor, cellSize * 2.05f, Offset(cellSize * 3f, cellSize * 3f))

    // Blue Base (Top Right)
    drawRect(blueColor, Offset(cellSize * 9, 0f), Size(cellSize * 6, cellSize * 6))
    drawCircle(Color(0xFFBBDEFB), cellSize * 2.3f, Offset(cellSize * 12f, cellSize * 3f))
    drawCircle(blueColor, cellSize * 2.05f, Offset(cellSize * 12f, cellSize * 3f))

    // Green Base (Bottom Left)
    drawRect(greenColor, Offset(0f, cellSize * 9), Size(cellSize * 6, cellSize * 6))
    drawCircle(Color(0xFFC8E6C9), cellSize * 2.3f, Offset(cellSize * 3f, cellSize * 12f))
    drawCircle(greenColor, cellSize * 2.05f, Offset(cellSize * 3f, cellSize * 12f))

    // Red Base (Bottom Right)
    drawRect(redColor, Offset(cellSize * 9, cellSize * 9), Size(cellSize * 6, cellSize * 6))
    drawCircle(Color(0xFFFFCDD2), cellSize * 2.3f, Offset(cellSize * 12f, cellSize * 12f))
    drawCircle(redColor, cellSize * 2.05f, Offset(cellSize * 12f, cellSize * 12f))

    // Base pawn spots circles
    drawBasePawnHoles(cellSize, 1f, 1f, Color.White)
    drawBasePawnHoles(cellSize, 10f, 1f, Color.White)
    drawBasePawnHoles(cellSize, 1f, 10f, Color.White)
    drawBasePawnHoles(cellSize, 10f, 10f, Color.White)

    // 2. Draw Track Grid lines
    for (x in 0..15) {
        drawLine(borderCol, Offset(x * cellSize, 0f), Offset(x * cellSize, 15 * cellSize), 0.8f)
    }
    for (y in 0..15) {
        drawLine(borderCol, Offset(0f, y * cellSize), Offset(15 * cellSize, y * cellSize), 0.8f)
    }

    // 3. Draw Colored Home Runways
    for (step in 1..5) {
        drawRect(yellowColor, Offset(step * cellSize, 7 * cellSize), Size(cellSize, cellSize))
        drawRect(blueColor, Offset(7 * cellSize, step * cellSize), Size(cellSize, cellSize))
        drawRect(redColor, Offset((14 - step) * cellSize, 7 * cellSize), Size(cellSize, cellSize))
        drawRect(greenColor, Offset(7 * cellSize, (14 - step) * cellSize), Size(cellSize, cellSize))
    }

    // Colored starting tiles
    drawRect(yellowColor, Offset(1 * cellSize, 6 * cellSize), Size(cellSize, cellSize))
    drawRect(blueColor, Offset(8 * cellSize, 1 * cellSize), Size(cellSize, cellSize))
    drawRect(redColor, Offset(13 * cellSize, 8 * cellSize), Size(cellSize, cellSize))
    drawRect(greenColor, Offset(6 * cellSize, 13 * cellSize), Size(cellSize, cellSize))

    // Safe star tiles with gray stars
    drawSafeStar(cellSize, 2f, 8f)
    drawSafeStar(cellSize, 6f, 2f)
    drawSafeStar(cellSize, 12f, 6f)
    drawSafeStar(cellSize, 8f, 12f)

    // 4. Center Home Triangles (3x3 cells in the center)
    val centerTopLeft = Offset(6 * cellSize, 6 * cellSize)
    val centerMid = Offset(7.5f * cellSize, 7.5f * cellSize)

    // Yellow Triangle (Left)
    val yellowPath = Path().apply {
        moveTo(centerTopLeft.x, centerTopLeft.y)
        lineTo(centerMid.x, centerMid.y)
        lineTo(centerTopLeft.x, centerTopLeft.y + 3 * cellSize)
        close()
    }
    drawPath(yellowPath, yellowColor)

    // Blue Triangle (Top)
    val bluePath = Path().apply {
        moveTo(centerTopLeft.x, centerTopLeft.y)
        lineTo(centerMid.x, centerMid.y)
        lineTo(centerTopLeft.x + 3 * cellSize, centerTopLeft.y)
        close()
    }
    drawPath(bluePath, blueColor)

    // Red Triangle (Right)
    val redPath = Path().apply {
        moveTo(centerTopLeft.x + 3 * cellSize, centerTopLeft.y)
        lineTo(centerMid.x, centerMid.y)
        lineTo(centerTopLeft.x + 3 * cellSize, centerTopLeft.y + 3 * cellSize)
        close()
    }
    drawPath(redPath, redColor)

    // Green Triangle (Bottom)
    val greenPath = Path().apply {
        moveTo(centerTopLeft.x, centerTopLeft.y + 3 * cellSize)
        lineTo(centerMid.x, centerMid.y)
        lineTo(centerTopLeft.x + 3 * cellSize, centerTopLeft.y + 3 * cellSize)
        close()
    }
    drawPath(greenPath, greenColor)

    // Center star / gold circle
    drawCircle(Color(0xFFFFD54F), cellSize * 0.7f, centerMid)
    drawCircle(Color(0xFFFFF9C4), cellSize * 0.4f, centerMid)
}

fun DrawScope.drawSafeStar(cellSize: Float, gridX: Float, gridY: Float) {
    val center = Offset((gridX + 0.5f) * cellSize, (gridY + 0.5f) * cellSize)
    drawCircle(Color(0xFFB0BEC5).copy(alpha = 0.5f), cellSize * 0.35f, center)
}

fun DrawScope.drawBasePawnHoles(cellSize: Float, startCellX: Float, startCellY: Float, color: Color) {
    val offsets = listOf(
        Offset((startCellX + 0.8f) * cellSize, (startCellY + 0.8f) * cellSize),
        Offset((startCellX + 2.8f) * cellSize, (startCellY + 0.8f) * cellSize),
        Offset((startCellX + 0.8f) * cellSize, (startCellY + 2.8f) * cellSize),
        Offset((startCellX + 2.8f) * cellSize, (startCellY + 2.8f) * cellSize)
    )
    for (pos in offsets) {
        drawCircle(color.copy(alpha = 0.35f), cellSize * 0.65f, pos)
        drawCircle(Color.White, cellSize * 0.5f, pos)
    }
}
