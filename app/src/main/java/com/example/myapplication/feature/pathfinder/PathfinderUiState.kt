package com.example.myapplication.feature.pathfinder

import com.example.myapplication.feature.pathfinder.model.CellState
import com.example.myapplication.feature.pathfinder.model.PathfinderConstants
import com.example.myapplication.feature.pathfinder.model.ToolMode

data class PathfinderUiState(
    val grid: List<CellState> = List(PathfinderConstants.TOTAL_CELLS) { CellState.EMPTY },
    val startIdx: Int = -1,
    val endIdx: Int = -1,
    val mode: ToolMode = ToolMode.START,
    val status: String = "Select a mode then tap a cell",
    val pathSteps: String = "--",
    val visitedCells: String = "--",
)
