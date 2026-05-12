package com.example.myapplication.feature.pathfinder.model

enum class ToolMode {
    START,
    END,
    WALL,
    ERASE,
    ;

    fun label(): String =
        when (this) {
            START -> "Start"
            END -> "End"
            WALL -> "Wall"
            ERASE -> "Erase"
        }
}
