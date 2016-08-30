# debugg.it #

## What is this repository for? ##

This is a library-project, which provides a tool to report your Android application bugs directly into your BitBucket Issue Tracker.

## How do I get set up? ##

### As module ###

Clone this repo.

Add this library into your project as module (`File -> New -> Import Module`).

### As `aar` & gradle dependency ###

Download `debuggit.aar` file from ...

Put your file in `libs` directory (`app/libs`). If you don't have this directory, create it.

Add these lines to your root (project) `build.gradle`:
```groovy
allprojects {
    repositories {
        ...
        flatDir {
           dirs 'libs'
        }
    }
}

```

Add this line to your app `build.gradle`:
```groovy
    compile(name:'debuggit', ext:'aar')
```

### Initialize debugg.it in your project ###

Init debugg.it in your `Application` class

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DebuggIt.getInstance().init("clientId", "secret", "repoName", "ownerName");
    }
}
```

To get your client ID and secret key, you must create **OAuth consumer** for your team (or individual user). You can find it in:

`Your Profile -> BitBucket settings -> ACCESS MANAGEMENT -> OAuth`

**This is already done for @MoodUp team, check line below.**

Owner name it's your team (or user) name.

For example, if you want to add issues to this repository, your `init()` should looks like
```java
DebuggIt.getInstance().init("Jz9hKhxwAWgRNcS6m8", "dzyS7K5mnvcEWFtsS6veUM8RDJxRzwXQ", "bugreporter", "moodup");
```

Attach BugReporter to your activities in `onStart()` method (it's perfect if your activities extends some `BaseActivity` class)

```java
public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ...
    }

    @Override
    protected void onStart() {
        super.onStart();
        DebuggIt.getInstance().attach(this);
    }

}
```

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