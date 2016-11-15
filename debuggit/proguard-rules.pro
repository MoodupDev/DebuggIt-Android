# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/michu/Library/Android/sdk/tools/proguard/proguard-android.txt
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

-keep class com.mooduplabs.debuggit.DebuggIt {
    public static <methods>;
    public void initBitbucket(...);
    public void initJira(...);
    public void initGitHub(...);
    public void attach(...);
    public void getScreenshotPermission(...);
}

-keepparameternames

-printmapping build/outputs/mapping/release/mapping.txt
