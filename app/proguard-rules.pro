# Xposed module (API 101)
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
    public void onModuleLoaded(...);
    public void onPackageLoaded(...);
    public void onPackageReady(...);
    public void onSystemServerStarting(...);
}

# Gesture handler invoked via Proxy
-keep class eu.hxreborn.tfs.gesture.GestureHandler { *; }

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

# Optional nullability annotation referenced by a transitive dependency.
-dontwarn javax.annotation.Nullable

# Obfuscation
-repackageclasses
-allowaccessmodification
