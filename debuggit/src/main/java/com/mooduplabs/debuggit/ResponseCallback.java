package com.mooduplabs.debuggit;

interface ResponseCallback<T> {
    void onSuccess(T response);

    void onFailure(int responseCode, String errorMessage);

    void onException(Exception ex);
}
