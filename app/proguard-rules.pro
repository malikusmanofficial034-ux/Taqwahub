# ProGuard / R8 Custom Security and Optimization Rules for TaqwaHub

# Retain Retrofit structures and annotations to prevent runtime serialization faults
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, MapSourceFile, LineNumberTable

-keepattributes RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations, RuntimeInvisibleParameterAnnotations

-keepclassmembers,allowobfuscation class * {
    @retrofit2.http.* <methods>;
}

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Keep Moshi JSON conversion classes from being obfuscated or removed
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Retain API entities and data models to prevent JSON desynchronization
-keep class com.example.data.api.** { *; }
-keep class com.example.data.models.** { *; }

# OkHttp & Okio rules for secure SSL handshake
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Jetpack Security / Crypto rules to prevent KeyRing encryption errors
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# AndroidX Room SQLite Keep rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep standard application entry points and services
-keep class com.example.TaqwaApplication { *; }
-keep class com.example.MainActivity { *; }
-keep class com.example.FirebaseMessagingService { *; }
-keep class com.example.util.PrayerAlarmReceiver { *; }
-keep class com.example.util.PrayerAlarmService { *; }
