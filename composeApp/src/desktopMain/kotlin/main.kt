package com.gongrab.menu

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.gongrab.menu.data.repository.MenuRepositoryImpl
import com.gongrab.menu.presentation.theme.GoNGrabTheme
import com.gongrab.menu.presentation.ui.MainAppScreen

fun main() = application {
    val repository = MenuRepositoryImpl()
    
    val windowState = rememberWindowState(
        size = DpSize(1200.dp, 800.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Go N Grab 24/7 - Menu Management (Compose Multiplatform macOS)",
        state = windowState
    ) {
        GoNGrabTheme {
            MainAppScreen(repository = repository)
        }
    }
}
