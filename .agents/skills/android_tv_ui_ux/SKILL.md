---
name: android_tv_ui_ux
description: Enforce world-class, native Android TV UI/UX design standards and guidelines for applications built with Compose for TV.
---

# Android TV UI/UX Standards and Guidelines (Compose for TV)

When developing or modifying Android TV applications using Compose Multiplatform or native Jetpack Compose, always adhere to the following strict guidelines to ensure a premium, accessible, and TV-native experience.

## 1. D-Pad Navigation & Focus Management (CRITICAL)
TV users navigate using a D-pad (Up, Down, Left, Right, Center).
- **NEVER rely on touch events** (`pointerdown`, `pointerup`, or un-focusable `click` events).
- **ALWAYS use Focusable components**: Utilize `androidx.tv.material3.Card`, `androidx.tv.material3.Button`, and `androidx.tv.foundation.lazy.list.TvLazyRow` / `TvLazyColumn` which handle D-pad focus states natively.
- **Focus Indication**: Focused items MUST scale up slightly (e.g., scale 1.1f) and feature a high-contrast border or glow to make it instantly obvious where the user is. Native TV Compose components do this automatically.
- **Focus Restoration**: Ensure that when navigating between screens, the focus doesn't get lost or reset ungracefully.

## 2. Immersive Fullscreen Experience
TV apps should feel like immersive media experiences, not desktop web pages.
- **Hide System UI**: Enforce immersive mode (hide status and navigation bars) in the `MainActivity` lifecycle. Use themes like `Theme.AppCompat.NoActionBar`.
- **Overscan Safe Zone**: TVs physically crop the edges of the screen (overscan). Always maintain a safe margin for critical UI elements: **58dp on the left/right** and **27dp on the top/bottom**.

## 3. Typography and Legibility
Users typically sit 10-15 feet away from the TV screen (the "10-foot UI").
- **Minimum Font Sizes**: Body text MUST be at least `14sp` (preferably `16sp` to `18sp`). Titles and headers should be `32sp` to `48sp`.
- **High Contrast**: Maintain extremely high contrast between text and backgrounds. Dark themes with vibrant neon or stark white text provide the best cinematic legibility.
- **Font Weights**: Avoid ultra-thin fonts. Regular, Medium, or Bold weights read much better from a distance.

## 4. Layouts and Data Presentation
- **Horizontal First**: Prefer horizontal scrolling (`TvLazyRow` or `Carousel`) for content categories, and vertical scrolling for exploring sections.
- **Grid Layouts**: Use `TvLazyVerticalGrid` for displaying menus (e.g., 4 to 5 columns max for landscape 16:9).
- **Low Density**: Do NOT cram too much information on the screen. Use ample whitespace. TV interfaces require breathing room.

## 5. Animations and Feedback
- **Micro-interactions**: When an item gains focus, smoothly animate the scale, elevation, and border opacity. Compose handles this easily via `Modifier.graphicsLayer` or `interactionSource`.
- **State Changes**: When data is fetching from Firebase/network, use skeleton loaders or smooth crossfades rather than abrupt pop-ins or unstyled text.
- **Avoid Web Hacks**: Never implement "double-tap" mechanics or rely on hover states. TV is exclusively focused-based navigation and single 'OK' button presses.

## 6. Performance Constraints
- TVs often have extremely low-end hardware (e.g., standard Fire TV Sticks).
- Minimize heavy recompositions.
- Use native Android Vector Drawables for icons instead of heavy raster images or expensive runtime SVG parsing.
- Cache data locally (DataStore/Room) so the app boots instantly even if the network is slow.
