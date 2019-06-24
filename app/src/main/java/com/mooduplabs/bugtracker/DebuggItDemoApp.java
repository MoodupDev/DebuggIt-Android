package com.mooduplabs.bugtracker;

import android.app.Application;

import com.mooduplabs.debuggit.ApiInterface;
import com.mooduplabs.debuggit.DebuggIt;
import com.mooduplabs.debuggit.JsonResponseCallback;

public class DebuggItDemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DebuggIt.getInstance().setRecordingEnabled(true);
        DebuggIt.getInstance().initS3("bucketName", " accessKey", "secretKey", "region");
        DebuggIt.getInstance().initApi("https://debuggit-api-staging.herokuapp.com", "/api/v1/upload/image", "/api/v1/upload/audio");

        DebuggIt.getInstance().initCustomApi(new ApiInterface() {
            @Override
            public void uploadImage(String imageData, String appId, JsonResponseCallback callback) {
                callback.onFailure(400, "test uploadImage");
            }

            @Override
            public void uploadAudio(String audioData, String appId, JsonResponseCallback callback) {
                callback.onFailure(400, "test uploadAudio");
            }
        });

        DebuggIt.getInstance().initBitbucket("bugreporter", "moodup");
    }
}