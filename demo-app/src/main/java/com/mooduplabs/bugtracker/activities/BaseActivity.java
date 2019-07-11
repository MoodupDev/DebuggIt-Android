package com.mooduplabs.bugtracker.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mooduplabs.debuggit.DebuggIt;

public abstract class BaseActivity extends AppCompatActivity {

    protected abstract int getLayout();

    protected abstract void onActivityReady(@Nullable Bundle savedInstanceState);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());

        onActivityReady(savedInstanceState);
    }

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

    public void runActivity(Class<?> className) {
        runActivity(className, null);
    }

    public void runActivity(Class<?> className, Bundle bundle) {
        Intent i = new Intent(this, className);

        if (bundle != null) {
            i.putExtras(bundle);
        }

        startActivity(i);
        finish();
    }
}
