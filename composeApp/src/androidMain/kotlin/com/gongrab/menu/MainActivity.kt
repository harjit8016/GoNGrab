package com.gongrab.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gongrab.menu.data.repository.MenuRepositoryImpl
import com.gongrab.menu.presentation.theme.GoNGrabTheme
import com.gongrab.menu.presentation.ui.MobileAppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = MenuRepositoryImpl()

        setContent {
            GoNGrabTheme {
                MobileAppScreen(repository = repository)
            }
        }
    }
}
