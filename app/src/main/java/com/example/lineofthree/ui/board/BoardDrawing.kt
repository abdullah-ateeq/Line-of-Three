package com.example.lineofthree.ui.board

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.util.lerp
import com.example.lineofthree.game.BoardRules
import com.example.lineofthree.game.Player
import com.example.lineofthree.ui.theme.BoardPalette
import com.example.lineofthree.ui.theme.TokenColors
import com.example.lineofthree.ui.theme.tokens
import kotlin.math.min

/** Maps normalized board coordinates to pixels for a square drawing area. */
class BoardGeometry(val side: Float, val origin: Offset = Offset.Zero) {
    val margin = side * MARGIN_FRACTION
    val span = side - margin * 2f
    val pieceRadius = side * 0.062f
    val socketRadius = side * 0.021f
    val hitSize = side * 0.18f
    val lineWidth = (side * 0.0055f).coerceAtLeast(1.5f)

    fun position(nodeId: Int): Offset {
        val n = BoardRules.nodes[nodeId]
        return Offset(origin.x + margin + n.x * span, origin.y + margin + n.y * span)
    }

    companion object {
        const val MARGIN_FRACTION = 0.1f

        fun of(size: Size) = min(size.width, size.height).let { side ->
            BoardGeometry(side, Offset((size.width - side) / 2f, (size.height - side) / 2f))
        }
    }
}

fun DrawScope.drawBoardPlate(geo: BoardGeometry, palette: BoardPalette, alpha: Float = 1f) {
    val corner = CornerRadius(geo.side * 0.045f)
    val plateSize = Size(geo.side, geo.side)
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(palette.plateTop, palette.plateBottom),
            startY = geo.origin.y,
            endY = geo.origin.y + geo.side
        ),
        topLeft = geo.origin,
        size = plateSize,
        cornerRadius = corner,
        alpha = alpha
    )
    // Soft vignette keeps the eye on the play area.
    drawRoundRect(
        brush = Brush.radialGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f)),
            center = geo.origin + Offset(geo.side / 2f, geo.side / 2f),
            radius = geo.side * 0.75f
        ),
        topLeft = geo.origin,
        size = plateSize,
        cornerRadius = corner,
        alpha = alpha
    )
    val inset = geo.side * 0.028f
    drawRoundRect(
        color = palette.plateEdge,
        topLeft = geo.origin + Offset(inset, inset),
        size = Size(geo.side - inset * 2, geo.side - inset * 2),
        cornerRadius = CornerRadius(geo.side * 0.03f),
        style = Stroke(width = geo.lineWidth * 0.6f),
        alpha = alpha
    )
    drawRoundRect(
        color = palette.lineHighlight,
        topLeft = geo.origin,
        size = plateSize,
        cornerRadius = corner,
        style = Stroke(width = 1.2f),
        alpha = alpha
    )
}

/** Engraved connections: a dark groove with a faint lit edge beneath the bronze inlay. */
fun DrawScope.drawConnections(geo: BoardGeometry, palette: BoardPalette, alpha: Float = 1f) {
    val w = geo.lineWidth
    BoardRules.edges.forEach { (a, b) ->
        val pa = geo.position(a)
        val pb = geo.position(b)
        drawLine(Color.Black.copy(alpha = 0.35f * alpha), pa + Offset(0f, w * 0.8f), pb + Offset(0f, w * 0.8f), w * 1.6f, StrokeCap.Round)
        drawLine(palette.line.copy(alpha = 0.85f * alpha), pa, pb, w, StrokeCap.Round)
        drawLine(palette.lineHighlight.copy(alpha = palette.lineHighlight.alpha * alpha), pa - Offset(0f, w * 0.35f), pb - Offset(0f, w * 0.35f), w * 0.35f, StrokeCap.Round)
    }
    // Central emblem: a small engraved lozenge inside the inner square.
    val c = geo.origin + Offset(geo.side / 2f, geo.side / 2f)
    val e = geo.side * 0.045f
    rotate(45f, pivot = c) {
        drawRect(palette.emblem.copy(alpha = palette.emblem.alpha * alpha), c - Offset(e, e), Size(e * 2, e * 2), style = Stroke(w * 0.8f))
    }
    drawCircle(palette.emblem.copy(alpha = palette.emblem.alpha * alpha), e * 0.28f, c)
}

fun DrawScope.drawSocket(center: Offset, geo: BoardGeometry, palette: BoardPalette, alpha: Float = 1f) {
    val r = geo.socketRadius
    drawCircle(palette.socketRim.copy(alpha = alpha), r * 1.35f, center + Offset(0f, r * 0.18f))
    drawCircle(
        brush = Brush.radialGradient(
            listOf(palette.socket, palette.socket.copy(alpha = 0.85f), palette.socketRim),
            center = center - Offset(0f, r * 0.25f),
            radius = r * 1.3f
        ),
        radius = r,
        center = center,
        alpha = alpha
    )
}

/**
 * A bevelled token with a soft drop shadow and an engraved symbol — a diamond for
 * Player 1, a ring for Player 2 — so players never depend on color alone.
 */
fun DrawScope.drawToken(
    center: Offset,
    radius: Float,
    player: Player,
    lift: Float = 0f,
    alpha: Float = 1f,
    colors: TokenColors = player.tokens()
) {
    if (alpha <= 0f || radius <= 0f) return
    val r = radius
    val shadowOffset = r * lerp(0.14f, 0.34f, lift)
    val shadowRadius = r * lerp(1.18f, 1.34f, lift)
    val shadowCenter = center + Offset(0f, shadowOffset)
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color.Black.copy(alpha = 0.55f * alpha), Color.Black.copy(alpha = 0.18f * alpha), Color.Transparent),
            center = shadowCenter,
            radius = shadowRadius
        ),
        radius = shadowRadius,
        center = shadowCenter
    )

    val c = center - Offset(0f, r * 0.1f * lift)
    // Body
    drawCircle(
        brush = Brush.radialGradient(
            listOf(colors.light, colors.base, colors.dark),
            center = c + Offset(-r * 0.35f, -r * 0.45f),
            radius = r * 1.7f
        ),
        radius = r,
        center = c,
        alpha = alpha
    )
    // Bevel: the face is slightly recessed — dark upper lip, lit lower lip.
    drawCircle(
        brush = Brush.verticalGradient(
            listOf(colors.dark.copy(alpha = 0.75f), colors.light.copy(alpha = 0.55f)),
            startY = c.y - r * 0.78f,
            endY = c.y + r * 0.78f
        ),
        radius = r * 0.76f,
        center = c,
        style = Stroke(width = r * 0.07f),
        alpha = alpha
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(colors.base, lerpColor(colors.base, colors.dark, 0.35f)),
            center = c - Offset(0f, r * 0.2f),
            radius = r * 0.9f
        ),
        radius = r * 0.72f,
        center = c,
        alpha = alpha
    )
    // Engraved symbol
    when (player) {
        Player.PLAYER_ONE -> {
            val d = r * 0.38f
            val path = Path().apply {
                moveTo(c.x, c.y - d)
                lineTo(c.x + d * 0.78f, c.y)
                lineTo(c.x, c.y + d)
                lineTo(c.x - d * 0.78f, c.y)
                close()
            }
            translate(top = r * 0.04f) { drawPath(path, colors.dark.copy(alpha = 0.6f * alpha)) }
            drawPath(path, colors.symbol.copy(alpha = 0.92f * alpha))
        }
        Player.PLAYER_TWO -> {
            val rr = r * 0.32f
            val sw = r * 0.12f
            drawCircle(colors.dark.copy(alpha = 0.6f * alpha), rr, c + Offset(0f, r * 0.04f), style = Stroke(sw))
            drawCircle(colors.symbol.copy(alpha = 0.92f * alpha), rr, c, style = Stroke(sw))
        }
    }
    // Specular sheen and crisp edge
    drawOval(
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.22f * alpha), Color.Transparent),
            startY = c.y - r * 0.92f,
            endY = c.y - r * 0.2f
        ),
        topLeft = Offset(c.x - r * 0.62f, c.y - r * 0.92f),
        size = Size(r * 1.24f, r * 0.7f)
    )
    drawCircle(colors.dark.copy(alpha = 0.7f * alpha), r, c, style = Stroke(width = r * 0.045f))
}

fun DrawScope.drawSelectionRing(center: Offset, radius: Float, color: Color, strokeWidth: Float, alpha: Float = 1f) {
    drawCircle(color.copy(alpha = color.alpha * alpha), radius, center, style = Stroke(strokeWidth))
}

fun DrawScope.drawDashedRing(center: Offset, radius: Float, color: Color, strokeWidth: Float, phase: Float) {
    val circumference = (2 * Math.PI * radius).toFloat()
    val dash = circumference / 16f
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash * 0.55f, dash * 0.45f), phase * dash)
        )
    )
}

/** Draws a segment through the three nodes of a line, trimmed to [progress] from its center. */
fun DrawScope.drawLineAccent(geo: BoardGeometry, lineId: Int, color: Color, width: Float, progress: Float = 1f) {
    val nodes = BoardRules.validLines[lineId]
    val start = geo.position(nodes.first())
    val end = geo.position(nodes.last())
    val mid = geo.position(nodes[1])
    val p = progress.coerceIn(0f, 1f)
    drawLine(color, mid + (start - mid) * p, mid + (end - mid) * p, width, StrokeCap.Round)
}

private fun lerpColor(a: Color, b: Color, t: Float) = Color(
    red = lerp(a.red, b.red, t),
    green = lerp(a.green, b.green, t),
    blue = lerp(a.blue, b.blue, t),
    alpha = lerp(a.alpha, b.alpha, t)
)
