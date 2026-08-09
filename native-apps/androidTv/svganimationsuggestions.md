When using Jetpack Compose, wrapping a WebView inside an AndroidView introduces a rendering conflict. When layout shifts occur (e.g., standard Compose state changes, window resizing, or AnimatedVisibility), Compose triggers recomposition and remeasuring. This forces the underlying WebView canvas to drop its internal Chromium animation clock, causing your SVG to freeze mid-frame.Apply these specific structural changes to your Compose code to keep the SVG animating smoothly during layout modifications:1. Avoid Putting Code in the update BlockThe update lambda of an AndroidView runs every single time a surrounding state triggers recomposition. Re-applying data, settings, or layout properties inside this lambda forces the WebView to redraw its canvas surface, instantly freezing running vector timelines.The Fix: Isolate the data loading code entirely inside the factory lambda so it only executes exactly once when the view mounts.kotlinAndroidView(
    modifier = Modifier.fillMaxWidth().height(250.dp),
    factory = { context ->
        WebView(context).apply {
            // Apply rendering configs exactly once
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            settings.javaScriptEnabled = true

            // Load your SVG HTML context safely here
            loadDataWithBaseURL(null, htmlSvgString, "text/html", "UTF-8", null)
        }
    },
    update = {
        // LEAVE EMPTY. Do not push data updates here during layout changes.
    }
)
Use code with caution.2. Wrap the WebView in a Native FrameLayoutDirectly resizing or moving an AndroidView containing a WebView forces a harsh Android requestLayout() loop down to the underlying native View tree, breaking active frame loops. Wrapping it inside a native container shields the WebView layout measurements from the Compose layer.kotlinAndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { context ->
        FrameLayout(context).apply {
            addView(WebView(context).apply {
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                settings.javaScriptEnabled = true
                loadDataWithBaseURL(null, htmlSvgString, "text/html", "UTF-8", null)
            }, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
        }
    }
)
Use code with caution.3. Isolate Shifting Layouts with graphicsLayerIf the layout change causes the position, padding, or alpha value of your composable wrapper to shift, Compose defaults to measuring the view boundaries again. This breaks Chromium's rendering loop.The Fix: Force animations or position changes to occur strictly on the GPU rendering layer by utilizing a graphicsLayer modifier. This shifts layouts visually without triggering a recalculation of the view boundaries.kotlin// Example: If changing alpha or translation during layout changes, do it like this:
AndroidView(
    modifier = Modifier
        .graphicsLayer(
            alpha = if (isLayoutExpanded) 1f else 0.5f,
            translationY = if (isLayoutExpanded) 0f else 50f
        )
        .size(200.dp),
    factory = { context -> /*... WebView setup ...*/ }
)
Use code with caution.4. Stabilize Multi-Item Lists with keyIf your SVG elements are inside a LazyColumn or a custom layout loop, a structural layout adjustment might cause Compose to mistake your components for brand-new elements. This causes it to completely destroy and recreate the underlying view canvas.The Fix: Pass a definitive structural identification factor to your loop items using key():kotlinLazyColumn {
    items(itemsList, key = { item -> item.uniqueId }) { item ->
        // Your layout containing the WebView
        SvgWebViewComponent(item.svgData)
    }
}
Use code with caution.To fix this instantly: What Compose layout elements wrap this WebView (e.g., LazyColumn, AnimatedVisibility, or a Box that changes size)? Sharing how your state triggers the layout change will help tailor the exact modifier parameters you need.
