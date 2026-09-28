package com.example.lineofthree.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.lineofthree.game.BoardRules
import com.example.lineofthree.game.Player
import com.example.lineofthree.ui.theme.LocalBoardPalette

/** A static, non-interactive board used for diagrams and decoration. */
@Composable
fun BoardPreview(
    pieces: Map<Int, Player>,
    modifier: Modifier = Modifier,
    highlightLines: Set<Int> = emptySet(),
    markedNodes: Set<Int> = emptySet(),
    description: String? = null
) {
    val palette = LocalBoardPalette.current
    val semantics = if (description != null) Modifier.semantics { contentDescription = description } else Modifier
    Canvas(modifier.then(semantics)) {
        val geo = BoardGeometry.of(size)
        drawBoardPlate(geo, palette)
        drawConnections(geo, palette)
        highlightLines.forEach { drawLineAccent(geo, it, palette.accent, geo.lineWidth * 1.6f) }
        BoardRules.nodeIds.forEach { drawSocket(geo.position(it), geo, palette) }
        pieces.forEach { (id, player) -> drawToken(geo.position(id), geo.pieceRadius, player) }
        markedNodes.forEach {
            drawDashedRing(geo.position(it), geo.pieceRadius * 1.28f, Color(0xFFD9786B), geo.lineWidth * 0.8f, 0f)
        }
    }
}
