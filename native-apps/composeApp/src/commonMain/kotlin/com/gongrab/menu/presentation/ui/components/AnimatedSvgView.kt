package com.gongrab.menu.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AnimatedSvgView(
    svgContent: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 36
)
