package com.mooduplabs.debuggit;

import org.json.JSONObject;

interface JsonResponseCallback extends ResponseCallback<JSONObject> {
    @Override
    void onSuccess(JSONObject response);
}
