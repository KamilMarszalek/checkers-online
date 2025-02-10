package pw.checkers

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import pw.checkers.models.initialBoard
import pw.checkers.ui.Board

@Composable
fun App() {
    println("sad")
    MaterialTheme {
        Board(initialBoard, emptySet()) { x, y -> println("$x $y") }
    }
}