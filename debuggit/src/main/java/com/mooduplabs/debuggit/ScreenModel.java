package com.mooduplabs.debuggit;

class ScreenModel {
    private String title;
    private String url;
    private boolean isLandscape;

    public ScreenModel(String title, String url, boolean isLandscape) {
        this.title = title;
        this.url = url;
        this.isLandscape = isLandscape;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public boolean isLandscape() {
        return isLandscape;
    }
}