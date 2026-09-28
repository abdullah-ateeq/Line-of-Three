package com.example.lineofthree.game

/**
 * A single point on the board. Coordinates are normalized to 0f..1f and are mapped
 * to screen space by the renderer.
 */
data class BoardNode(
    val id: Int,
    val x: Float,
    val y: Float,
    val neighbors: Set<Int>
)

/**
 * The fixed 16-node board: an outer square and an inner square (corners + midpoints),
 * joined by eight spokes (midpoint-to-midpoint and corner-to-corner).
 *
 * Node numbering runs clockwise from the top-left corner of each square:
 *
 * ```
 *  0 ---------- 1 ---------- 2
 *  |  \         |         /  |
 *  |    8 ----- 9 ----- 10   |
 *  |    |               |    |
 *  7 - 15               11 - 3
 *  |    |               |    |
 *  |    14 ---- 13 ---- 12   |
 *  |  /         |         \  |
 *  6 ---------- 5 ---------- 4
 * ```
 *
 * All geometry and rule definitions live here and nowhere else.
 */
object BoardRules {

    const val NODE_COUNT = 16

    private val positions: List<Pair<Float, Float>> = listOf(
        // Outer square
        0.00f to 0.00f, // 0
        0.50f to 0.00f, // 1
        1.00f to 0.00f, // 2
        1.00f to 0.50f, // 3
        1.00f to 1.00f, // 4
        0.50f to 1.00f, // 5
        0.00f to 1.00f, // 6
        0.00f to 0.50f, // 7
        // Inner square
        0.25f to 0.25f, // 8
        0.50f to 0.25f, // 9
        0.75f to 0.25f, // 10
        0.75f to 0.50f, // 11
        0.75f to 0.75f, // 12
        0.50f to 0.75f, // 13
        0.25f to 0.75f, // 14
        0.25f to 0.50f  // 15
    )

    /** Explicit adjacency list. Every node has exactly three connections. */
    val adjacency: Map<Int, Set<Int>> = mapOf(
        0 to setOf(1, 7, 8),
        1 to setOf(0, 2, 9),
        2 to setOf(1, 3, 10),
        3 to setOf(2, 4, 11),
        4 to setOf(3, 5, 12),
        5 to setOf(4, 6, 13),
        6 to setOf(5, 7, 14),
        7 to setOf(6, 0, 15),
        8 to setOf(0, 9, 15),
        9 to setOf(1, 8, 10),
        10 to setOf(2, 9, 11),
        11 to setOf(3, 10, 12),
        12 to setOf(4, 11, 13),
        13 to setOf(5, 12, 14),
        14 to setOf(6, 13, 15),
        15 to setOf(7, 14, 8)
    )

    /**
     * Every legal line of three. Each entry is ordered end-to-middle-to-end so the
     * renderer can draw it as one straight segment. The list index is the line ID.
     */
    val validLines: List<List<Int>> = listOf(
        listOf(0, 1, 2),     // outer top
        listOf(2, 3, 4),     // outer right
        listOf(4, 5, 6),     // outer bottom
        listOf(6, 7, 0),     // outer left
        listOf(8, 9, 10),    // inner top
        listOf(10, 11, 12),  // inner right
        listOf(12, 13, 14),  // inner bottom
        listOf(14, 15, 8)    // inner left
    )

    val nodes: List<BoardNode> = positions.mapIndexed { id, (x, y) ->
        BoardNode(id = id, x = x, y = y, neighbors = adjacency.getValue(id))
    }

    val nodeIds: IntRange = 0 until NODE_COUNT

    /** Unique undirected connections, used for rendering. */
    val edges: List<Pair<Int, Int>> = adjacency.flatMap { (a, ns) ->
        ns.filter { it > a }.map { a to it }
    }

    fun neighbors(nodeId: Int): Set<Int> = adjacency[nodeId].orEmpty()

    fun isAdjacent(a: Int, b: Int): Boolean = b in neighbors(a)
}
