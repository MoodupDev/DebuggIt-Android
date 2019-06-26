package com.mooduplabs.bugtracker;

import android.app.Application;

import com.mooduplabs.debuggit.ApiInterface;
import com.mooduplabs.debuggit.DebuggIt;
import com.mooduplabs.debuggit.JsonResponseCallback;

import org.json.JSONException;
import org.json.JSONObject;

public class DebuggItDemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DebuggIt.getInstance()
                .setRecordingEnabled(true)
                .configureDefaultApi("https://url-to-backend.com/api", "/debuggit/uploadImage", "/debuggit/uploadAudio")
                .configureCustomApi(new ApiInterface() {
                    @Override
                    public void uploadImage(String imageData, String appId, JsonResponseCallback callback) {
                        JSONObject response = new JSONObject();

                        try {
                            response.put("url", "https://via.placeholder.com/150C");
                            callback.onSuccess(response);
                        } catch (JSONException ex) {
                            callback.onFailure(400, "Could not upload image");
                        }
                    }

                    @Override
                    public void uploadAudio(String audioData, String appId, JsonResponseCallback callback) {
                        JSONObject response = new JSONObject();

                        try {
                            response.put("url", "");
                            callback.onSuccess(response);
                        } catch (JSONException ex) {
                            callback.onFailure(400, "Could not upload audio");
                        }
                    }
                })
                .configureS3Bucket("bucketName", "accessKey", "secretKey", "eu-central-1")
                .configureBitbucket("repositoryName", "accountName")
                .init();
    }
}