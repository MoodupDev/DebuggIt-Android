-android

-keepparameternames

-keep public class com.mooduplabs.debuggit.DebuggIt {
    public static <methods>;
    public com.mooduplabs.debuggit.DebuggIt configureBitbucket(...);
    public com.mooduplabs.debuggit.DebuggIt configureJira(...);
    public com.mooduplabs.debuggit.DebuggIt configureGitHub(...);
    public com.mooduplabs.debuggit.DebuggIt configureS3Bucket(...);
    public com.mooduplabs.debuggit.DebuggIt configureDefaultApi(...);
    public com.mooduplabs.debuggit.DebuggIt configureCustomApi(...);
    public com.mooduplabs.debuggit.DebuggIt setRecordingEnabled(...);
    public void init(...);
    public void attach(...);
    public void getScreenshotPermission(...);
}

-keep public interface com.mooduplabs.debuggit.ApiInterface { *; }
-keep public interface com.mooduplabs.debuggit.JsonResponseCallback { *; }
-keep interface com.mooduplabs.debuggit.ResponseCallback { *; }

-printmapping build/outputs/mapping/release/mapping.txt