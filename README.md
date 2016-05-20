# BugReporter #

## What is this repository for? ##

This is a library-project, which provides a tool to report your Android application bugs directly into your BitBucket Issue Tracker.

## How do I get set up? ##

Clone this repo.

Add this library into your project as module (`File -> New -> Import Module`).

Init BugReporter in your `Application` class

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BugReporter.getInstance().init("clientId", "repoName", "ownerName");
    }
}
```

To get your client ID, you must create **OAuth consumer** for your team (or individual user). You can find it in:

`Your Profile -> BitBucket settings -> ACCESS MANAGEMENT -> OAuth`

**This is already done for @MoodUp team, check line below.**

Owner name it's your team (or user) name.

For example, if you want to add issues to this repository, your `init()` should looks like
```java
BugReporter.getInstance().init("C9PnuH4fPyvUDjFwMz", "bugreporter", "moodup");
```

Attach BugReporter to your activities in `onStart()` method (it's perfect if your activities extends some `BaseActivity` class)

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ...
    }

    @Override
    protected void onStart() {
        super.onStart();
        BugReporter.getInstance().attach(this);
    }

}
```

## That's all. Your BugReporter is ready to work. ##