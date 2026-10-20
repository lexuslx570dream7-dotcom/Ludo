# ProGuard & R8 Optimization Rules for Ludo Club

# Unity Ads SDK Rules
-keep class com.unity3d.ads.** { *; }
-keep interface com.unity3d.ads.** { *; }
-keep class com.unity3d.services.** { *; }
-keep interface com.unity3d.services.** { *; }
-dontwarn com.unity3d.services.**
-dontwarn com.unity3d.ads.**

# App Models, Room & ViewModel preservation
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** { *; }
-keep class com.example.ads.** { *; }
-keepclassmembers class com.example.ads.** { *; }
-keep class com.example.data.** { *; }
-keep class com.example.viewmodel.** { *; }

# Moshi & Retrofit Serialization
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-dontwarn com.squareup.moshi.**

# Firebase & Credential Manager
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
