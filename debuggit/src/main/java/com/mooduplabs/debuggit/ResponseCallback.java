package com.mooduplabs.debuggit;

interface ResponseCallback<T> {
    void onSuccess(T response);
    void onFailure(int responseCode, T errorMessage);
}
