# Keep data models for Gson
-keepclassmembers class com.kundaliai.app.data.models.** { *; }
-keep class com.kundaliai.app.data.models.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# iText PDF
-dontwarn com.itextpdf.**
-keep class com.itextpdf.** { *; }

# Billing
-keep class com.android.billingclient.** { *; }

# AdMob
-keep class com.google.android.gms.ads.** { *; }

# Lottie
-keep class com.airbnb.lottie.** { *; }

# Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
