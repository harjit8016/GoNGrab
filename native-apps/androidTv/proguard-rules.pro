# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/harjitsingh/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and flags by changing the proguardFiles
# attribute in build.gradle.

# Keep Compose/Kotlin runtime classes intact
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Firebase/Firestore models if necessary
-keep class com.google.firebase.** { *; }
-keep class com.gitlive.firebase.** { *; }

# Keep standard WebView/JavaScript interface rules
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
