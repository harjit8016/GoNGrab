-keep class com.gongrab.** { *; }

# Suppress warnings for desktop-only AWT classes not available on Android
-dontwarn java.awt.FileDialog
-dontwarn java.awt.Frame
-dontwarn java.awt.**

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }

