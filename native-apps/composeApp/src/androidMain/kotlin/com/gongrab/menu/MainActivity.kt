package com.gongrab.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import com.gongrab.menu.data.repository.FirebaseMenuRepositoryImpl
import com.gongrab.menu.data.repository.MenuRepositoryImpl
import com.gongrab.menu.domain.repository.MenuRepository
import com.gongrab.menu.presentation.theme.GoNGrabTheme
import com.gongrab.menu.presentation.ui.MobileAppScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository: MenuRepository = try {
            val options = FirebaseOptions(
                projectId = "grabngo-b5778",
                applicationId = "1:104883703989340978347:android:com_gongrab_menu",
                apiKey = "AIzaSyGrabNGoAppKey"
            )
            try {
                Firebase.initialize(this, options)
            } catch (e: Exception) {
                // Already initialized or platform note
            }
            FirebaseMenuRepositoryImpl()
        } catch (e: Exception) {
            println("Falling back to MenuRepositoryImpl: ${e.message}")
            MenuRepositoryImpl()
        }

        setContent {
            GoNGrabTheme {
                MobileAppScreen(repository = repository)
            }
        }
    }
}
