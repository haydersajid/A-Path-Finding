package com.example.myapplication


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// ─────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────

private const val COLS = 10
private const val ROWS = 12

// ─────────────────────────────────────────────
// Enums
// ─────────────────────────────────────────────

enum class CellState { EMPTY, WALL, PATH, VISITED }

enum class ToolMode {
    START, END, WALL, ERASE;

    fun label(): String = when (this) {
        START -> "Start"
        END   -> "End"
        WALL  -> "Wall"
        ERASE -> "Erase"
    }

    fun activeColor(): Color = when (this) {
        START -> Color(0xFF4CAF50)
        END   -> Color(0xFFF44336)
        WALL  -> Color(0xFF546E7A)
        ERASE -> Color(0xFFFF9800)
    }
}

// ─────────────────────────────────────────────
// Activity
// ─────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF2F2F2)
                ) {
                    AStarApp()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────

@Composable
fun AStarApp() {
    val totalCells = COLS * ROWS
    val grid = remember { mutableStateListOf(*Array(totalCells) { CellState.EMPTY }) }
    var startIdx     by remember { mutableStateOf(-1) }
    var endIdx       by remember { mutableStateOf(-1) }
    var mode         by remember { mutableStateOf(ToolMode.START) }
    var status       by remember { mutableStateOf("Select a mode then tap a cell") }
    var pathSteps    by remember { mutableStateOf("--") }
    var visitedCells by remember { mutableStateOf("--") }
    val wallCount = grid.count { it == CellState.WALL }

    fun clearPathAndVisited() {
        for (i in grid.indices) {
            if (grid[i] == CellState.PATH || grid[i] == CellState.VISITED)
                grid[i] = CellState.EMPTY
        }
    }

    fun handleTap(index: Int) {
        if (index < 0 || index >= totalCells) return
        clearPathAndVisited()
        pathSteps    = "--"
        visitedCells = "--"
        when (mode) {
            ToolMode.START -> {
                if (index == endIdx) return
                startIdx = index
                status = "Start placed. Now set End or draw Walls."
            }
            ToolMode.END -> {
                if (index == startIdx) return
                endIdx = index
                status = "End placed. Draw walls or tap Find Path."
            }
            ToolMode.WALL -> {
                if (index == startIdx || index == endIdx) return
                grid[index] = if (grid[index] == CellState.WALL) CellState.EMPTY else CellState.WALL
                status = "Wall toggled. Tap Find Path when ready."
            }
            ToolMode.ERASE -> {
                when (index) {
                    startIdx -> { startIdx = -1; status = "Start removed." }
                    endIdx   -> { endIdx   = -1; status = "End removed." }
                    else     -> { grid[index] = CellState.EMPTY; status = "Cell cleared." }
                }
            }
        }
    }

    fun findPath() {
        if (startIdx < 0) { status = "Place a Start point first!"; return }
        if (endIdx   < 0) { status = "Place an End point first!";  return }
        clearPathAndVisited()

        val result = aStar(
            start  = startIdx,
            goal   = endIdx,
            cols   = COLS,
            rows   = ROWS,
            isWall = { grid[it] == CellState.WALL }
        )

        result.visited.forEach { i ->
            if (i != startIdx && i != endIdx) grid[i] = CellState.VISITED
        }
        result.path.forEach { i ->
            if (i != startIdx && i != endIdx) grid[i] = CellState.PATH
        }

        pathSteps    = if (result.path.isEmpty()) "--" else "${result.path.size - 1}"
        visitedCells = "${result.visited.size}"
        status = if (result.path.isEmpty())
            "No path found. Try removing some walls."
        else
            "Path found! ${result.path.size - 1} steps, ${result.visited.size} cells visited."
    }

    fun resetAll() {
        for (i in grid.indices) grid[i] = CellState.EMPTY
        startIdx     = -1
        endIdx       = -1
        mode         = ToolMode.START
        pathSteps    = "--"
        visitedCells = "--"
        status       = "Board reset. Select a mode then tap a cell."
    }

    // ── Layout ──────────────────────────────────────────────────────────────

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {

        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EE))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text = "A* Pathfinder",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Visualize shortest path finding",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }

        // Tool mode selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(ToolMode.START, ToolMode.END, ToolMode.WALL, ToolMode.ERASE).forEach { m ->
                val isActive = mode == m
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isActive) m.activeColor().copy(alpha = 0.15f)
                            else Color(0xFFF5F5F5)
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isActive) m.activeColor() else Color(0xFFDDDDDD),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { mode = m }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = m.label(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) m.activeColor() else Color(0xFF555555),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(COLS),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFEEEEEE))
                .padding(6.dp),
            verticalArrangement   = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            userScrollEnabled = false
        ) {
            itemsIndexed(grid) { index, cellState ->
                val isStart = index == startIdx
                val isEnd   = index == endIdx
                val bgColor = when {
                    isStart                        -> Color(0xFF4CAF50)
                    isEnd                          -> Color(0xFFF44336)
                    cellState == CellState.WALL    -> Color(0xFF37474F)
                    cellState == CellState.PATH    -> Color(0xFF2196F3)
                    cellState == CellState.VISITED -> Color(0xFFBBDEFB)
                    else                           -> Color(0xFFE0E0E0)
                }
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(bgColor)
                        .clickable { handleTap(index) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isStart) Text("S", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    if (isEnd)   Text("E", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StatChip("Steps",   pathSteps,         Color(0xFF6200EE), Modifier.weight(1f))
            StatChip("Visited", visitedCells,       Color(0xFF2196F3), Modifier.weight(1f))
            StatChip("Walls",   wallCount.toString(), Color(0xFF37474F), Modifier.weight(1f))
        }

        // Find Path + Reset buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick  = { findPath() },
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Find Path", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick  = { resetAll() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reset", fontSize = 14.sp)
            }
        }

        // Status bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6200EE))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = status,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9F9F9))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendDot(Color(0xFF4CAF50), "Start")
            LegendDot(Color(0xFFF44336), "End")
            LegendDot(Color(0xFF2196F3), "Path")
            LegendDot(Color(0xFFBBDEFB), "Visited")
            LegendDot(Color(0xFF37474F), "Wall")
        }
    }
}

// ─────────────────────────────────────────────
// Reusable composables
// ─────────────────────────────────────────────

@Composable
fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 9.sp, color = Color(0xFF888888))
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(label, fontSize = 9.sp, color = Color(0xFF666666))
    }
}

// ─────────────────────────────────────────────
// A* Algorithm
// ─────────────────────────────────────────────

data class AStarResult(
    val path: List<Int>,
    val visited: List<Int>
)

fun aStar(
    start: Int,
    goal: Int,
    cols: Int,
    rows: Int,
    isWall: (Int) -> Boolean
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
            r to (c + 1)
        )
            .filter { (nr, nc) -> nr in 0 until rows && nc in 0 until cols }
            .map    { (nr, nc) -> nr * cols + nc }
            .filter { !isWall(it) }
    }

    val openSet      = mutableListOf(start)
    val cameFrom     = mutableMapOf<Int, Int>()
    val gScore       = mutableMapOf(start to 0).withDefault { Int.MAX_VALUE }
    val fScore       = mutableMapOf(start to heuristic(start, goal)).withDefault { Int.MAX_VALUE }
    val visitedOrder = mutableListOf<Int>()
    val closedSet    = mutableSetOf<Int>()

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
            val tentativeG = gScore.getValue(current).let {
                if (it == Int.MAX_VALUE) Int.MAX_VALUE else it + 1
            }
            if (tentativeG < gScore.getValue(nb)) {
                cameFrom[nb] = current
                gScore[nb]   = tentativeG
                fScore[nb]   = tentativeG + heuristic(nb, goal)
                if (nb !in openSet) openSet.add(nb)
            }
        }
    }

    return AStarResult(path = emptyList(), visited = visitedOrder)
}
