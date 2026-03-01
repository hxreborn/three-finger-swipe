# Module entry point
-keep class eu.hxreborn.tfs.xposed.ThreeFingerSwipeModule { *; }

# All hooker classes
-keep @io.github.libxposed.api.annotations.XposedHooker class * { *; }

# Keep annotations
-keep class io.github.libxposed.api.annotations.** { *; }
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations

# Hook methods
-keepclassmembers class * {
    @io.github.libxposed.api.annotations.BeforeInvocation <methods>;
    @io.github.libxposed.api.annotations.AfterInvocation <methods>;
}

# Xposed module class pattern
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onPackageLoaded(...);
    public void onSystemServerLoaded(...);
}

# Gesture handler invoked via Proxy
-keep class eu.hxreborn.tfs.gesture.ThreeFingerGestureHandler { *; }

# Xposed detection method
-keep class eu.hxreborn.tfs.ui.MainActivity {
    public static boolean isXposedEnabled();
}

# Kotlin intrinsics optimization
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void check*(...);
    public static void throw*(...);
}
-assumenosideeffects class java.util.Objects {
    public static ** requireNonNull(...);
}

# Strip debug logs in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Obfuscation
-repackageclasses
-allowaccessmodification
