# debugg.it #

[https://debugg.it](https://debugg.it)

## Table of Contents ##

+ [What is this repository for?](#what-is-this)
+ [How do I get set up?](#setup)
    * [As a gradle dependency](#setup-gradle)
    * [As a module](#setup-module)
+ [Configure and initialize debugg.it in your project](#configure)
    * [Create a class which extends Application class](#configure-create-application-class)
    * [Add newly created application class name into your AndroidManifest.xml file](#configure-manifest)
    * [Add permissions to AndroidManifest.xml file](#configure-permissions)
    * [Configure and init debugg.it in Application class](#configure-application-class)
    * [Attach debugg.it to Activity class](#configure-activity-class)
+ [Additional options](#extra-options)
+ [Licence](#licence)

<a name="what-is-this"/>

## What is this repository for? ##

This is a library-project, which provides a tool to report Android application bugs directly into JIRA / GitHub/ BitBucket Issue Tracker.

<a name="setup"/>

## How do I get set up? ##

You can set up debugg.it as gradle dependency or as a module.

<a name="setup-gradle"/>

### As a gradle dependency ###

Add these lines to your module's `build.gradle` file:

```groovy
repositories {
    jcenter()
}
```

```groovy
dependencies {
    implementation 'com.mooduplabs:debuggit:1.1.1'
}
```

<a name="setup-module"/>

### As a module ###

Clone this repo.

Add this library into your project as module (`File -> New -> Import Module`).

Add following lines to your app-level `build.gradle` file:

```groovy
dependencies {
    compile project(path: ':debuggit')
}
```
##### Configurations #####

**debugg.it** is delivered with 2 configurations: `debug` and `release`.

The `debug` configuration is not minified by ProGuard. If you want to use this configuration, add `configuration` parameter to your `compile project` method and set it to `'debug'`:

```groovy
dependencies {
    compile project(path: ':debuggit', configuration: 'debug')
}
```

##### Flavors #####

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
    stagingCompile project(path: ':debuggit', configuration: 'debug')
    productionCompile project(path: ':debuggit', configuration: 'release')
}
```

<a name="configure"/>

## Configure and initialize debugg.it in your project ##

1. Create a class which extends `Application` class
2. Add newly created application class name into your `AndroidManifest.xml` file
3. Add permissions to `AndroidManifest.xml` file
4. Configure and init debugg.it in `Application` class
5. Attach debugg.it to `Activity` class

<a name="configure-create-application-class"/>

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

<a name="configure-manifest"/>

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

<a name="configure-permissions"/>

### Add permissions to `AndroidManifest.xml` file ###

Add these permissions to `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

<a name="configure-application-class"/>

### Configure and init debugg.it in `Application` class ###

In `Application` class `onCreate()` method, you have to configure and init debugg.it. There are two things that needs to be configured:

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
            * `region` is a region where your S3 bucket is hosted

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

#### Sample configurations

+ Init BitBucket with S3:
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

<a name="configure-activity-class"/>

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

<a name="extra-options"/>

## Additional options

**debugg.it** allows to record audio notes and add it to bug description. To enable this feature simply add this line to your configuration in `Application` class:

```java
DebuggIt.getInstance().setRecordingEnabled(true);
```

Ensure you have added `RECORD_AUDIO` permission in `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

<a name="licence"/>

## Licence ##

```
Copyright 2019 MoodUp Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```