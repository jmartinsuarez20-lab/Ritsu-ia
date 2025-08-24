# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Ritsu AI specific classes
-keep class com.ritsu.ai.** { *; }
-keep class com.ritsu.ai.service.** { *; }
-keep class com.ritsu.ai.util.** { *; }
-keep class com.ritsu.ai.ui.** { *; }
-keep class com.ritsu.ai.data.** { *; }
-keep class com.ritsu.ai.viewmodel.** { *; }

# Keep accessibility service
-keep class com.ritsu.ai.service.RitsuAccessibilityService { *; }
-keep class com.ritsu.ai.service.RitsuCallHandlerService { *; }

# Keep AI and utility classes
-keep class com.ritsu.ai.util.AIManager { *; }
-keep class com.ritsu.ai.util.OPPOOptimizer { *; }
-keep class com.ritsu.ai.util.ColorOSOptimizer { *; }

# Keep application class
-keep class com.ritsu.ai.RitsuApplication { *; }