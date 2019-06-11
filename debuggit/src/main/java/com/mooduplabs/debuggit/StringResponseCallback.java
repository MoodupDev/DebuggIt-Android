package com.mooduplabs.debuggit;

interface StringResponseCallback extends ResponseCallback<String> {
    @Override
    void onSuccess(String response);
}
