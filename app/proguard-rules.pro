# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Reyansh  \AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn com.google.android.gms.**

-keep class com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class com.naman14.timber.lastfmapi.models.** { *; }
-keep class de.hdodenhof.circleimageview.CircleImageView.**{*;}
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }

#Retrofit
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn retrofit.**
-dontwarn retrofit2.**
-dontwarn rx.**

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

#UIL
-keep class com.nostra13.universalimageloader.** { *; }
-keepclassmembers class com.nostra13.universalimageloader.** {*;}

-keepattributes Exceptions
-keepattributes Signature
-keepattributes *Annotation*

#renderscript
-keep class android.support.v8.renderscript.** { *; }
-keep class android.support.v8.renderscript.** { *; }

#searchview
-keep class android.support.v7.widget.SearchView { *; }
#JAudioTagger
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**