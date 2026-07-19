# Xposed 模块不需要混淆
-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage
-keep class com.example.nokeybypass.** { *; }
