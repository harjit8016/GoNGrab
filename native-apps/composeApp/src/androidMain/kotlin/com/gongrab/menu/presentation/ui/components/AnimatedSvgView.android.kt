package com.gongrab.menu.presentation.ui.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
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

    val cleanHtml = remember(svgContent) {
        val rawSvg = if (!svgContent.trim().startsWith("<svg")) {
            "<svg viewBox=\"0 0 100 100\" fill=\"none\" xmlns=\"http://www.w3.org/2000/svg\">$svgContent</svg>"
        } else svgContent

        val dynamicallyCleaned = rawSvg.replace(
            Regex("<rect\\s+[^>]*?(?:width=[\"'](?:100%|100|500|1000)[\"'])[^>]*?/?>", RegexOption.IGNORE_CASE)
        ) { match ->
            if (match.value.contains("x=\"0\"") || match.value.contains("y=\"0\"") || match.value.contains("width=\"100%\"") || match.value.contains("width=\"100\"")) "" else match.value
        }

        """
            <!DOCTYPE html>
            <html style="background: transparent !important; overflow: hidden; width: 100%; height: 100%;">
            <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; background: transparent !important; }
                html, body {
                    margin: 0;
                    padding: 0;
                    background: transparent !important;
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
                    display: block;
                    background: transparent !important;
                    border: none !important;
                    outline: none !important;
                    overflow: visible !important;
                    filter: drop-shadow(0 3px 6px rgba(0, 0, 0, 0.55));
                }
            </style>
            </head>
            <body style="background: transparent !important; overflow: hidden;">
                ${dynamicallyCleaned.replace("<svg ", "<svg style=\"background: transparent !important; overflow: visible;\" ")}
            </body>
            </html>
        """.trimIndent()
    }

    Box(modifier = modifier.size(sizeDp.dp)) {
        AndroidView(
            modifier = Modifier.size(sizeDp.dp),
            factory = { context ->
                FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val webView = WebView(context).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        isVerticalScrollBarEnabled = false
                        isHorizontalScrollBarEnabled = false
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            allowContentAccess = true
                            useWideViewPort = false
                            loadWithOverviewMode = false
                        }
                        webViewClient = WebViewClient()
                    }
                    addView(webView)
                }
            },
            update = { container ->
                val webView = container.getChildAt(0) as? WebView
                if (webView != null && cleanHtml.isNotBlank()) {
                    if (webView.tag != cleanHtml) {
                        webView.tag = cleanHtml
                        webView.loadDataWithBaseURL(null, cleanHtml, "text/html", "UTF-8", null)
                    }
                }
            },
            onRelease = { container ->
                val webView = container.getChildAt(0) as? WebView
                webView?.apply {
                    stopLoading()
                    loadUrl("about:blank")
                    webChromeClient = null
                    clearHistory()
                    removeAllViews()
                    destroy()
                }
                container.removeAllViews()
            }
        )
    }
}
