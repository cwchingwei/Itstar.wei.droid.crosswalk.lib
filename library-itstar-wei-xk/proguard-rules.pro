-keepattributes org.xwalk.core.JavascriptInterface
-keepclassmembers class library.itstar.wei.xk.local.WebAppInterface {
   public *;
}

-keepclassmembers class * {
    @org.xwalk.core.JavascriptInterface <methods>;
}

-dontwarn org.xwalk.core.**
-keep class org.xwalk.core.**{*;}
-dontwarn org.apache.cordova.**
-keep class org.apache.cordova.**{*;}
-dontwarn org.chromium.**
-keep class org.chromium.**{*;}
-keepattributes **