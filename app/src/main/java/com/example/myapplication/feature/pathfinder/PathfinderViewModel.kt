package com.example.myapplication.feature.pathfinder

import androidx.lifecycle.ViewModel
import com.example.myapplication.feature.pathfinder.model.CellState
import com.example.myapplication.feature.pathfinder.model.PathfinderConstants
import com.example.myapplication.feature.pathfinder.model.ToolMode
import com.example.myapplication.feature.pathfinder.model.aStar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PathfinderViewModel : ViewModel() {

    private val totalCells = PathfinderConstants.TOTAL_CELLS
    private val cols = PathfinderConstants.COLS
    private val rows = PathfinderConstants.ROWS

    private val _uiState = MutableStateFlow(PathfinderUiState())
    val uiState: StateFlow<PathfinderUiState> = _uiState.asStateFlow()

    fun setMode(mode: ToolMode) {
        _uiState.update { it.copy(mode = mode) }
    }

    fun onCellTap(index: Int) {
        if (index < 0 || index >= totalCells) return
        _uiState.update { state ->
            var grid = clearPathAndVisited(state.grid)
            var startIdx = state.startIdx
            var endIdx = state.endIdx
            var status = state.status

            when (state.mode) {
                ToolMode.START -> {
                    if (index == endIdx) {
                        return@update state.copy(
                            grid = grid,
                            pathSteps = "--",
                            visitedCells = "--",
                        )
                    }
                    startIdx = index
                    status = "Start placed. Now set End or draw Walls."
                }
                ToolMode.END -> {
                    if (index == startIdx) {
                        return@update state.copy(
                            grid = grid,
                            pathSteps = "--",
                            visitedCells = "--",
                        )
                    }
                    endIdx = index
                    status = "End placed. Draw walls or tap Find Path."
                }
                ToolMode.WALL -> {
                    if (index == startIdx || index == endIdx) {
                        return@update state.copy(
                            grid = grid,
                            pathSteps = "--",
                            visitedCells = "--",
                        )
                    }
                    grid = grid.toMutableList().apply {
                        this[index] =
                            if (this[index] == CellState.WALL) CellState.EMPTY else CellState.WALL
                    }
                    status = "Wall toggled. Tap Find Path when ready."
                }
                ToolMode.ERASE -> {
                    when (index) {
                        startIdx -> {
                            startIdx = -1
                            status = "Start removed."
                        }
                        endIdx -> {
                            endIdx = -1
                            status = "End removed."
                        }
                        else -> {
                            grid = grid.toMutableList().apply { this[index] = CellState.EMPTY }
                            status = "Cell cleared."
                        }
                    }
                }
            }

            state.copy(
                grid = grid,
                startIdx = startIdx,
                endIdx = endIdx,
                pathSteps = "--",
                visitedCells = "--",
                status = status,
            )
        }
    }

    fun onFindPath() {
        _uiState.update { state ->
            if (state.startIdx < 0) {
                return@update state.copy(status = "Place a Start point first!")
            }
            if (state.endIdx < 0) {
                return@update state.copy(status = "Place an End point first!")
            }

            var grid = clearPathAndVisited(state.grid)
            val startIdx = state.startIdx
            val endIdx = state.endIdx

            val result =
                aStar(
                    start = startIdx,
                    goal = endIdx,
                    cols = cols,
                    rows = rows,
                    isWall = { i -> grid[i] == CellState.WALL },
                )

            val g = grid.toMutableList()
            result.visited.forEach { i ->
                if (i != startIdx && i != endIdx) g[i] = CellState.VISITED
            }
            result.path.forEach { i ->
                if (i != startIdx && i != endIdx) g[i] = CellState.PATH
            }

            val pathSteps =
                if (result.path.isEmpty()) "--" else "${result.path.size - 1}"
            val visitedCells = "${result.visited.size}"
            val status =
                if (result.path.isEmpty()) {
                    "No path found. Try removing some walls."
                } else {
                    "Path found! ${result.path.size - 1} steps, ${result.visited.size} cells visited."
                }

            state.copy(
                grid = g,
                pathSteps = pathSteps,
                visitedCells = visitedCells,
                status = status,
            )
        }
    }

    fun onReset() {
        _uiState.value =
            PathfinderUiState(
                grid = List(totalCells) { CellState.EMPTY },
                mode = ToolMode.START,
                status = "Board reset. Select a mode then tap a cell.",
            )
    }

    private fun clearPathAndVisited(grid: List<CellState>): List<CellState> =
        grid.map { cell ->
            if (cell == CellState.PATH || cell == CellState.VISITED) CellState.EMPTY else cell
        }
}
