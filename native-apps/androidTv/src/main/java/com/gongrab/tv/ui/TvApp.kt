package com.gongrab.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.gongrab.menu.domain.model.Branch
import com.gongrab.menu.domain.repository.MenuRepository

@Composable
fun TvApp(repository: MenuRepository) {
    var selectedBranch by remember { mutableStateOf<Branch?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedBranch == null) {
            BranchSelectionScreen(
                repository = repository,
                onBranchSelected = { branch -> selectedBranch = branch }
            )
        } else {
            MenuBoardScreen(
                repository = repository,
                branch = selectedBranch!!,
                onBack = { selectedBranch = null }
            )
        }
    }
}
