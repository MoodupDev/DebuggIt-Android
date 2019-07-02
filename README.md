# debugg.it #

## Table of Contents ##

[TOC]

## What is this repository for? ##

This is a library-project, which provides a tool to report your Android application bugs directly into your BitBucket Issue Tracker.

## How do I get set up? ##

You can set up debugg.it as module or as `aar` & gradle dependency.

### As module ###

Clone this repo.

Add this library into your project as module (`File -> New -> Import Module`).

Add to your app-level `build.gradle` file in `dependencies` method this line:

```groovy
dependencies {
    //...
    compile project(path: ':debuggit')
}
```
##### Configurations #####

**debugg.it** is delivered with 2 configurations: `debug` and `release`. 

The `debug` configuration is not minified by ProGuard. If you want to use this configuration, add `configuration` parameter to your `compile project` method and set it to `'debug'`:

```groovy
dependencies {
    //...
    compile project(path: ':debuggit', configuration: 'debug')
}
```

##### Flavours #####

If your project has many product flavors, you can use this syntax:

```groovy
    productFlavors {
        staging {
            applicationId "com.example.app.staging"
        }

        production {
            applicationId "com.example.app"
        }
    }

dependencies {
    //...
    stagingCompile project(path: ':debuggit', configuration: 'debug')
    productionCompile project(path: ':debuggit', configuration: 'release')
}
```

### As `aar` & gradle dependency ###

Download `debuggit.aar` file from [here](http://debugg.it/downloads/android/0.6.0/debuggit.aar).

Put your file in `libs` directory (`<your project path>/app/libs`). If you don't have this directory, create it.

Add these lines to your `app/build.gradle` file:
```groovy
repositories {
    ...
    flatDir {
       dirs 'libs'
    }
}

...

dependencies {
    ...
    compile(name:'debuggit', ext:'aar')

}

```

Sync your gradle files.

## Configure and initialize debugg.it in your project ##

1. Create a class which extends `Application` class
2. Add newly created application class name into your `AndroidManifest.xml` file
3. Add permissions to `AndroidManifest.xml` file
4. Configure and init debugg.it in Application class
5. Attach debugg.it to `Activity` class

### Create a class which extends `Application` class ###

This is required so that we can init debugg.it together with application.

##### Example #####
```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
```

### Add newly created application class name into your `AndroidManifest.xml` file ###

If your new class is `MyApplication.class`:

```xml
<manifest … >

  <uses-permission … />
  <uses-permission … />

  <application
    android:name=".MyApplication"
    … >
      <activity … />
      <activity … />
   </application>
</manifest>
```

### Add permissions to `AndroidManifest.xml` file ###

Add these permissions to `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Configure and init debugg.it in Application class ###

In Application `onCreate()` method, you have to configure and init debugg.it. There are two things that needs to be configured:

+ Service where issues will be reported
+ API for uploading image files and (optionally) audio files

##### Configure service for issues #####

debugg.it currently supports BitBucket, GitHub and JIRA.

+ BitBucket
    * `DebuggIt.getInstance().configureBitbucket("repoName", "ownerName");`
    * where
        * `repoName` is your **repository name**
        * `ownerName` is username of **repository owner**
    * [Enable an issue tracker](https://confluence.atlassian.com/bitbucket/enable-an-issue-tracker-223216498.html) on the Bitbucket repository that will use debugg.it

+ GitHub
    * `DebuggIt.getInstance().configureGitHub("repoName", "ownerName");`
    * where
        * `repoName` is your **repository name**
        * `ownerName` is username of **repository owner**

+ JIRA
    * `DebuggIt.getInstance().configureJira("host", "projectKey");`
    * where
        * `host` hostname of your **JIRA server** (e.g. `yourcompany.atlassian.net`)
        * `projectKey` is **key** of your project
    * If your host don't use **SSL** use this method:
      `DebuggIt.getInstance().configureJira("host", "projectKey", false);`

##### API for uploading image files and (optionally) audio files #####

debugg.it requires an API where it can send image files and (optionally) audio files. There are 3 available configurations:

+ AWS S3 Bucket

    This configuration uses your AWS S3 bucket (https://aws.amazon.com/s3/) to store image and audio files.

    * `DebuggIt.getInstance().configureS3Bucket("bucketName", "accessKey", "secretKey", "region");`
        * where
            * `bucketName` is a name of your bucket (e.g. `https://url-to-backend.com`)
            * `accessKey` is your user's access key
            * `secretKey` is your user's secret key
            * `region` is a region where your S3 bucket is hosted. It can be either a `String` or a `Region` from `com.amazonaws.regions.Region` AWS core SDK dependency.

    We recommend that you create a separate user for debugg.it via AWS Identity and Access Management (IAM) with Read and Write Access to your S3 bucket.
##

+ Default API

    This configuration uses your backend to send image and audio files.

    * `DebuggIt.getInstance().configureDefaultApi("baseUrl", "uploadImageEndpoint", "uploadAudioEndpoint");`
    * where
        * `baseUrl` is a base url of your backend (e.g. `https://url-to-backend.com`)
        * `uploadImageEndpoint` is an url to endpoint handling image upload (e.g. `/debuggit/uploadImage`)
        * `uploadAudioEndpoint` is an url to endpoint handling audio upload (e.g. `/debuggit/uploadAudio`)
##

+ Custom API

    This is an extension of default API configuration. The difference is that you have to handle `uploadImage` / `uploadAudio` request and response. You are responsible for communication with your backend, but at the same time you have full control over it.

    * `DebuggIt.getInstance().configureCustomApi(apiInterface);`
    * where
        * `apiInterface` is an interface that has two methods: `uploadImage` and `uploadAudio`
            * `public void uploadImage(String imageData, JsonResponseCallback callback);`
            * `public void uploadAudio(String audioData, JsonResponseCallback callback);`
            * where
                * `imageData` / `audioData` is a Base64 encoded bitmap / audio file.
                * `callback` is a Base64 encoded bitmap / audio file.

##### Init debugg.it #####

After configuration is done, call at the end:

`DebuggIt.getInstance().init();`

### Additional options

**debugg.it** allows to record audio notes and add it to bug description. To enable this feature simply add this line to your configuration in `Application` class:

```java
DebuggIt.getInstance().setRecordingEnabled(true);

```

Ensure you have added `RECORD_AUDIO` permission in `AndroidManifest.xml` file:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### Sample configurations

+ Init BitBucket with S3 and audio recordings:
##
```java
DebuggIt.getInstance()
        .configureS3Bucket("bucketName", "accessKey", "secretKey", "region");
        .configureBitbucket("repoName", "ownerName");
        .init();
```

+ Init GitHub with default API:
##
```java
DebuggIt.getInstance()
        .configureDefaultApi("baseUrl", "uploadImageEndpoint", "uploadAudioEndpoint");
        .configureGitHub("host", "projectKey");
        .init();
```

+ Init JIRA with custom API:
##
```java
DebuggIt.getInstance()
        .configureCustomApi(new ApiInterface() {
                            @Override
                            public void uploadImage(String imageData, JsonResponseCallback callback) {
                                // Handle API call to your backend and call onSuccess callback with JSONObject response
                                // callback.onSuccess(response);

                                // If something went wrong, call onFailure callback
                                // callback.onFailure(400, "Could not upload image");
                            }

                            @Override
                            public void uploadAudio(String audioData, JsonResponseCallback callback) {
                                // Handle API call to your backend and call onSuccess callback with JSONObject response
                                // callback.onSuccess(response);

                                // If something went wrong, call onFailure callback
                                // callback.onFailure(400, "Could not upload audio");
                            }
                        })
        .configureJira("host", "projectKey");
        .init();
```

### Attach debugg.it to `Activity` class ###

Add these methods in your `Activity` classes (preferably just in base activity class)

* Add this method to `onStart()`:

`DebuggIt.getInstance().attach(this);`

* Add this method to `onActivityResult()`:

`DebuggIt.getInstance().getScreenshotPermission(requestCode, resultCode, data);`

##### Example #####
```java
public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();
        DebuggIt.getInstance().attach(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DebuggIt.getInstance().getScreenshotPermission(requestCode, resultCode, data);
    }
}
```

## That's all. Your debugg.it is ready to work. ##