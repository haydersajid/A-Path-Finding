package com.example.myapplication.feature.pathfinder.model

import kotlin.math.abs

fun aStar(
    start: Int,
    goal: Int,
    cols: Int,
    rows: Int,
    isWall: (Int) -> Boolean,
): AStarResult {
    fun toRow(i: Int) = i / cols
    fun toCol(i: Int) = i % cols

    fun heuristic(a: Int, b: Int) =
        abs(toRow(a) - toRow(b)) + abs(toCol(a) - toCol(b))

    fun neighbors(i: Int): List<Int> {
        val r = toRow(i)
        val c = toCol(i)
        return listOf(
            (r - 1) to c,
            (r + 1) to c,
            r to (c - 1),
            r to (c + 1),
        )
            .filter { (nr, nc) -> nr in 0 until rows && nc in 0 until cols }
            .map { (nr, nc) -> nr * cols + nc }
            .filter { !isWall(it) }
    }

    val openSet = mutableListOf(start)
    val cameFrom = mutableMapOf<Int, Int>()
    val gScore = mutableMapOf(start to 0).withDefault { Int.MAX_VALUE }
    val fScore = mutableMapOf(start to heuristic(start, goal)).withDefault { Int.MAX_VALUE }
    val visitedOrder = mutableListOf<Int>()
    val closedSet = mutableSetOf<Int>()

    while (openSet.isNotEmpty()) {
        val current = openSet.minByOrNull { fScore.getValue(it) }!!

        if (current == goal) {
            val path = mutableListOf(current)
            var c = current
            while (c in cameFrom) {
                c = cameFrom[c]!!
                path.add(c)
            }
            return AStarResult(path = path.reversed(), visited = visitedOrder)
        }

        openSet.remove(current)
        closedSet.add(current)
        visitedOrder.add(current)

        for (nb in neighbors(current)) {
            if (nb in closedSet) continue
            val tentativeG =
                gScore.getValue(current).let {
                    if (it == Int.MAX_VALUE) Int.MAX_VALUE else it + 1
                }
            if (tentativeG < gScore.getValue(nb)) {
                cameFrom[nb] = current
                gScore[nb] = tentativeG
                fScore[nb] = tentativeG + heuristic(nb, goal)
                if (nb !in openSet) openSet.add(nb)
            }
        }
    }

    return AStarResult(path = emptyList(), visited = visitedOrder)
}
