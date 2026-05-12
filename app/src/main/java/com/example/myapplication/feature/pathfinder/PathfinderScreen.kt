package com.example.myapplication.feature.pathfinder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.feature.pathfinder.model.CellState
import com.example.myapplication.feature.pathfinder.model.PathfinderConstants
import com.example.myapplication.feature.pathfinder.model.ToolMode

@Composable
fun PathfinderScreen(viewModel: PathfinderViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val wallCount = uiState.grid.count { it == CellState.WALL }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6200EE))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Column {
                Text(
                    text = "A* Pathfinder",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Visualize shortest path finding",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(ToolMode.START, ToolMode.END, ToolMode.WALL, ToolMode.ERASE).forEach { m ->
                val isActive = uiState.mode == m
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isActive) m.activeColor().copy(alpha = 0.15f)
                                else Color(0xFFF5F5F5),
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isActive) m.activeColor() else Color(0xFFDDDDDD),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .clickable { viewModel.setMode(m) }
                            .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = m.label(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) m.activeColor() else Color(0xFF555555),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(PathfinderConstants.COLS),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFEEEEEE))
                    .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            userScrollEnabled = false,
        ) {
            itemsIndexed(uiState.grid) { index, cellState ->
                val isStart = index == uiState.startIdx
                val isEnd = index == uiState.endIdx
                val bgColor =
                    when {
                        isStart -> Color(0xFF4CAF50)
                        isEnd -> Color(0xFFF44336)
                        cellState == CellState.WALL -> Color(0xFF37474F)
                        cellState == CellState.PATH -> Color(0xFF2196F3)
                        cellState == CellState.VISITED -> Color(0xFFBBDEFB)
                        else -> Color(0xFFE0E0E0)
                    }
                Box(
                    modifier =
                        Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(bgColor)
                            .clickable { viewModel.onCellTap(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isStart) {
                        Text("S", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    if (isEnd) {
                        Text("E", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            StatChip("Steps", uiState.pathSteps, Color(0xFF6200EE), Modifier.weight(1f))
            StatChip("Visited", uiState.visitedCells, Color(0xFF2196F3), Modifier.weight(1f))
            StatChip("Walls", wallCount.toString(), Color(0xFF37474F), Modifier.weight(1f))
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { viewModel.onFindPath() },
                modifier =
                    Modifier
                        .weight(2f)
                        .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            ) {
                Text("Find Path", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = { viewModel.onReset() },
                modifier =
                    Modifier
                        .weight(1f)
                        .height(48.dp),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Reset", fontSize = 14.sp)
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6200EE))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = uiState.status,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendDot(Color(0xFF4CAF50), "Start")
            LegendDot(Color(0xFFF44336), "End")
            LegendDot(Color(0xFF2196F3), "Path")
            LegendDot(Color(0xFFBBDEFB), "Visited")
            LegendDot(Color(0xFF37474F), "Wall")
        }
    }
}

private fun ToolMode.activeColor(): Color =
    when (this) {
        ToolMode.START -> Color(0xFF4CAF50)
        ToolMode.END -> Color(0xFFF44336)
        ToolMode.WALL -> Color(0xFF546E7A)
        ToolMode.ERASE -> Color(0xFFFF9800)
    }

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.08f))
                .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 9.sp, color = Color(0xFF888888))
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
        )
        Text(label, fontSize = 9.sp, color = Color(0xFF666666))
    }
}
