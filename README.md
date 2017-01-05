# debugg.it #

## What is this repository for? ##

This is a library-project, which provides a tool to report your Android application bugs directly into your BitBucket Issue Tracker.

## How do I get set up? ##

### As module ###

Clone this repo.

Add this library into your project as module (`File -> New -> Import Module`).

### As `aar` & gradle dependency ###

Download `debuggit.aar` file from [here](http://debugg.it/downloads/debuggit.aar).

Put your file in `libs` directory (`<your project path>/app/libs`). If you don't have this directory, create it.

Add these lines to your `build.gradle`:
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

### Initialize debugg.it in your project ###

Create class which extends `Application` class.

Add application name into your `AndroidManifest.xml` file

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

Add these permissions to `AndroidManifest.xml` file

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

Add one of this methods to your Application `onCreate()` method

+ Bitbucket
    * `DebuggIt.getInstance().initBitbucket("repoName", "ownerName");`
    * where
        * `repoName` is **your repository name**
        * `ownerName` is **username of repository owner**
    * [Enable an issue tracker](https://confluence.atlassian.com/bitbucket/enable-an-issue-tracker-223216498.html) on the Bitbucket repository that will use debugg.it

+ GitHub
    * `DebuggIt.getInstance().initGitHub("repoName", "ownerName");`
    * where
        * `repoName` is **your repository name**
        * `ownerName` is **username of repository owner**

+ JIRA
    * `DebuggIt.getInstance().initJira("host", "projectKey");`
    * where
        * `host` hostname of your **JIRA server** (e.g. `yourcompany.atlassian.net`)
        * `projectKey` is **key** of your project
    * If your host don't use **SSL** use this method:
      `DebuggIt.getInstance().initJira("host", "projectKey", false);`

Add these methods in your `Activity` classes

* Add this method to `onStart()` method of your activities 
  `DebuggIt.getInstance().attach(this);`

* Add this method to `onActivityResult()` method of your activities
  `DebuggIt.getInstance().getScreenshotPermission(requestCode, resultCode, data);`

### Include dialogs, popups etc.

You can take screenshot with your custom or system dialogs for devices with **Android API > 20**. To get this working, you must add this line to your `Activity` `onActivityResult` method:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    DebuggIt.getInstance().getScreenshotPermission(requestCode, resultCode, data);
}

```

## That's all. Your debugg.it is ready to work. ##