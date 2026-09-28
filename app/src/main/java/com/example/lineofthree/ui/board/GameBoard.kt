package com.example.lineofthree.ui.board

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.example.lineofthree.game.BoardRules
import com.example.lineofthree.game.GameEngine
import com.example.lineofthree.game.GamePhase
import com.example.lineofthree.game.LineOfThreeState
import com.example.lineofthree.game.Player
import com.example.lineofthree.ui.GameUiState
import com.example.lineofthree.ui.GameViewModel
import com.example.lineofthree.ui.theme.LocalBoardPalette
import com.example.lineofthree.ui.theme.tokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val RemoveColor = Color(0xFFD9786B)

@Composable
fun GameBoard(
    ui: GameUiState,
    showLegalMoves: Boolean,
    highlightCompletedLines: Boolean,
    onNodeTap: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = ui.game
    val palette = LocalBoardPalette.current

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseValue"
    )
    val dashPhase by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "dashPhase"
    )

    // Line-formed flash: quick draw-in, two gentle pulses, then fade.
    val lineFlash = remember { Animatable(1f) }
    LaunchedEffect(ui.actionId, state.newLineIds) {
        if (state.newLineIds.isNotEmpty() && state.phase == GamePhase.REMOVE) {
            lineFlash.snapTo(0f)
            lineFlash.animateTo(1f, tween(1400, easing = LinearEasing))
        } else {
            lineFlash.snapTo(1f)
        }
    }

    val motionProgress = remember { Animatable(1f) }
    LaunchedEffect(ui.motion?.id) {
        if (ui.motion != null) {
            motionProgress.snapTo(0f)
            motionProgress.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
        }
    }

    val placeProgress = remember { Animatable(1f) }
    LaunchedEffect(ui.actionId, ui.placedNodeId) {
        if (ui.placedNodeId != null) {
            placeProgress.snapTo(0f)
            placeProgress.animateTo(1f, tween(190, easing = FastOutSlowInEasing))
        }
    }

    val vanishProgress = remember { Animatable(1f) }
    LaunchedEffect(ui.vanishing, ui.actionId) {
        if (ui.vanishing != null) {
            vanishProgress.snapTo(0f)
            vanishProgress.animateTo(1f, tween(GameViewModel.REMOVE_ANIMATION_MS.toInt(), easing = LinearEasing))
        }
    }

    val selectLift = remember { Animatable(0f) }
    LaunchedEffect(state.selectedNodeId) {
        if (state.selectedNodeId != null) {
            selectLift.snapTo(0f)
            selectLift.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
        } else {
            selectLift.snapTo(0f)
        }
    }

    val completedLines = remember(state.pieces) {
        Player.entries.associateWith { GameEngine.getCompletedLineIds(state.pieces, it) }
    }
    val placementTargets = state.phase == GamePhase.PLACEMENT && !state.isGameOver

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(14.dp, RoundedCornerShape(percent = 5), clip = false)
    ) {
        val density = LocalDensity.current
        val sidePx = with(density) { maxWidth.toPx() }

        Canvas(Modifier.fillMaxSize()) {
            val geo = BoardGeometry.of(size)
            val t = lineFlash.value
            val flashDraw = (t / 0.2f).coerceIn(0f, 1f)
            val flashFade = 1f - ((t - 0.65f) / 0.35f).coerceIn(0f, 1f)
            val flashPulse = 0.72f + 0.28f * cos(((t - 0.2f).coerceAtLeast(0f) / 0.8f) * 4f * PI.toFloat())
            val removeFloor = if (state.phase == GamePhase.REMOVE) 0.55f else 0f
            val flashIntensity = maxOf(flashPulse * flashFade, removeFloor)

            drawBoardPlate(geo, palette)
            drawConnections(geo, palette)

            // Persistent completed-line accents
            if (highlightCompletedLines) {
                completedLines.forEach { (player, lines) ->
                    lines.filter { it !in state.newLineIds }.forEach { id ->
                        drawLineAccent(geo, id, player.tokens().light.copy(alpha = 0.55f), geo.lineWidth * 1.5f)
                    }
                }
            }
            // Newly formed line
            if (state.newLineIds.isNotEmpty() && (flashIntensity > 0f || highlightCompletedLines)) {
                val intensity = if (highlightCompletedLines) maxOf(flashIntensity, 0.55f) else flashIntensity
                state.newLineIds.forEach { id ->
                    drawLineAccent(geo, id, palette.accent.copy(alpha = 0.25f * intensity), geo.lineWidth * 5f, flashDraw)
                    drawLineAccent(geo, id, palette.accent.copy(alpha = intensity), geo.lineWidth * 1.6f, flashDraw)
                }
            }

            // Sockets and hints
            BoardRules.nodeIds.forEach { id ->
                val p = geo.position(id)
                drawSocket(p, geo, palette)
                if (id !in state.pieces) {
                    if (placementTargets && showLegalMoves) {
                        drawSelectionRing(p, geo.socketRadius * 2.1f, palette.accent.copy(alpha = 0.22f + 0.12f * pulse), geo.lineWidth * 0.6f)
                    }
                    if (id in state.legalDestinationIds && showLegalMoves) {
                        val tc = state.currentPlayer.tokens()
                        drawSelectionRing(p, geo.socketRadius * 2.4f, tc.light.copy(alpha = 0.7f), geo.lineWidth * 0.7f)
                        drawCircle(tc.light.copy(alpha = 0.9f), geo.socketRadius * 0.75f, p)
                    }
                }
            }

            // Glow beneath pieces of the newly formed line
            if (state.newLineIds.isNotEmpty() && flashIntensity > 0f) {
                state.newLineIds.flatMap { BoardRules.validLines[it] }.toSet().forEach { id ->
                    drawSelectionRing(
                        geo.position(id),
                        geo.pieceRadius * (1.2f + 0.06f * flashPulse),
                        palette.accent.copy(alpha = 0.55f * flashIntensity),
                        geo.lineWidth * 0.8f
                    )
                }
            }

            // Pieces — the selected piece is drawn last so it lifts above its neighbors.
            val order = state.pieces.keys.sortedBy { if (it == state.selectedNodeId) 1 else 0 }
            order.forEach { id ->
                val owner = state.pieces.getValue(id)
                var center = geo.position(id)
                var lift = 0f
                var radius = geo.pieceRadius
                var alpha = 1f

                val motion = ui.motion
                if (motion != null && motion.toNodeId == id && motionProgress.value < 1f) {
                    val mp = motionProgress.value
                    center = lerp(geo.position(motion.fromNodeId), center, mp)
                    lift = sin(PI.toFloat() * mp) * 0.8f
                }
                if (ui.placedNodeId == id && placeProgress.value < 1f) {
                    val pp = placeProgress.value
                    radius *= lerp(1.18f, 1f, pp)
                    lift = 1f - pp
                    alpha = (pp * 2.5f).coerceAtMost(1f)
                }
                if (id == state.selectedNodeId) lift = 0.9f * selectLift.value

                val removable = state.phase == GamePhase.REMOVE && id in state.removableNodeIds
                if (state.phase == GamePhase.REMOVE && owner != state.currentPlayer && !removable) alpha *= 0.55f

                drawToken(center, radius * (1f + 0.06f * lift), owner, lift, alpha)

                if (id == state.selectedNodeId) {
                    drawSelectionRing(center - Offset(0f, radius * 0.1f * lift), radius * 1.24f, palette.accent, geo.lineWidth * 0.7f, selectLift.value)
                }
                if (removable) {
                    drawDashedRing(center, radius * (1.26f + 0.04f * pulse), RemoveColor.copy(alpha = 0.65f + 0.35f * pulse), geo.lineWidth * 0.8f, dashPhase)
                }
            }

            // Removed piece: brief pulse, then shrink and fade.
            ui.vanishing?.let { v ->
                val vp = vanishProgress.value
                if (vp < 1f) {
                    val scale = if (vp < 0.3f) 1f + 0.12f * sin(PI.toFloat() * (vp / 0.3f)) else lerp(1f, 0.35f, (vp - 0.3f) / 0.7f)
                    val a = 1f - ((vp - 0.25f) / 0.75f).coerceIn(0f, 1f)
                    drawToken(geo.position(v.nodeId), geo.pieceRadius * scale, v.player, 0f, a)
                    drawSelectionRing(geo.position(v.nodeId), geo.pieceRadius * 1.3f * scale, RemoveColor.copy(alpha = a * 0.8f), geo.lineWidth * 0.8f)
                }
            }
        }

        // Accessible, generously sized tap targets over each node.
        val geo = BoardGeometry(sidePx)
        val hitDp = with(density) { geo.hitSize.toDp() }
        BoardRules.nodeIds.forEach { id ->
            val p = geo.position(id)
            val half = geo.hitSize / 2f
            Box(
                Modifier
                    .offset { IntOffset((p.x - half).roundToInt(), (p.y - half).roundToInt()) }
                    .size(hitDp)
                    .semantics {
                        contentDescription = describeNode(state, id)
                        nodeStateDescription(state, id, completedLines)?.let { stateDescription = it }
                    }
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        enabled = !state.isGameOver && !state.isAnimating,
                        role = Role.Button
                    ) { onNodeTap(id) }
            )
        }
    }
}

private fun describeNode(state: LineOfThreeState, id: Int): String {
    val owner = state.pieces[id]
    val node = "node ${id + 1}"
    return when {
        owner != null && state.phase == GamePhase.REMOVE && id in state.removableNodeIds ->
            "Removable ${owner.label} piece on $node"
        owner != null && id == state.selectedNodeId -> "Selected ${owner.label} piece on $node"
        owner != null -> "${owner.label} piece on $node"
        id in state.legalDestinationIds -> "Legal destination, $node"
        else -> "Empty board point, $node"
    }
}

private fun nodeStateDescription(
    state: LineOfThreeState,
    id: Int,
    completedLines: Map<Player, Set<Int>>
): String? {
    val owner = state.pieces[id] ?: return null
    val inLine = completedLines[owner].orEmpty().any { id in BoardRules.validLines[it] }
    return if (inLine) "Completed line" else null
}
