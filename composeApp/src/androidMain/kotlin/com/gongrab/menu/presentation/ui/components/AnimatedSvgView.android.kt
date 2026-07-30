package com.gongrab.menu.presentation.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    val cleanSvg = remember(svgContent) {
        if (!svgContent.trim().startsWith("<svg")) {
            "<svg viewBox=\"0 0 100 100\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\">$svgContent</svg>"
        } else svgContent
    }

    Box(modifier = modifier.size(sizeDp.dp)) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowContentAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }
                    webViewClient = WebViewClient()
                }
            },
            update = { webView ->
                val htmlData = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        html, body {
                            margin: 0;
                            padding: 0;
                            background: transparent !important;
                            width: 100vw;
                            height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            overflow: hidden;
                        }
                        svg {
                            width: 100%;
                            height: 100%;
                            display: block;
                        }
                    </style>
                    </head>
                    <body>
                        $cleanSvg
                    </body>
                    </html>
                """.trimIndent()

                webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
            },
            modifier = Modifier.size(sizeDp.dp)
        )
    }
}
