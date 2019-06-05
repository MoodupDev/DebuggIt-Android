package com.mooduplabs.debuggit;

public class ScreenModel {
    private String title;
    private String url;

    public ScreenModel(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

}