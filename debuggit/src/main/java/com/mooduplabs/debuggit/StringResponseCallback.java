package com.mooduplabs.debuggit;

public interface StringResponseCallback extends ResponseCallback<String> {

    @Override
    void onSuccess(String response);
}
