# libxposed module entry point
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowobfuscation,allowoptimization public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

-keep class io.github.libxposed.service.XposedProvider { *; }

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-dontwarn io.github.libxposed.api.**
-dontwarn io.github.libxposed.annotation.**

# not on the compile classpath
-dontwarn javax.annotation.Nullable

-repackageclasses
-allowaccessmodification
