package com.gongrab.menu.presentation.ui.components

import android.webkit.WebView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun AnimatedSvgView(
    svgContent: String,
    modifier: Modifier,
    sizeDp: Int
) {
    if (svgContent.isBlank()) return

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setInitialScale(100)
            }
        },
        update = { webView ->
            val htmlData = """
                <!DOCTYPE html>
                <html>
                <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    html, body {
                        margin: 0;
                        padding: 0;
                        background: transparent;
                        width: 100%;
                        height: 100%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        overflow: hidden;
                    }
                    svg {
                        width: 100%;
                        height: 100%;
                        max-width: ${sizeDp}px;
                        max-height: ${sizeDp}px;
                    }
                </style>
                </head>
                <body>
                    $svgContent
                </body>
                </html>
            """.trimIndent()

            webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
        },
        modifier = modifier.size(sizeDp.dp)
    )
}
