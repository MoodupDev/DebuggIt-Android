package com.moodup.bugreporter;

import java.util.ArrayList;
import java.util.List;

public class Report {

    private String title;
    private String content;
    private String kind;
    private String priority;
    private List<String> audioUrls;
    private List<String> screensUrls;

    public Report() {
        this.audioUrls = new ArrayList<>();
        this.screensUrls = new ArrayList<>();
    }

    protected String getTitle() {
        return title == null ? "" : title;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    protected String getContent() {
        return content == null ? "" : content;
    }

    protected void setContent(String content) {
        this.content = content;
    }

    protected List<String> getAudioUrls() {
        return audioUrls;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    protected void setAudioUrls(List<String> audioUrls) {
        this.audioUrls = audioUrls;
    }

    protected List<String> getScreensUrls() {
        return screensUrls;
    }

    protected void setScreensUrls(List<String> screensUrls) {
        this.screensUrls = screensUrls;
    }

    protected void clear() {
        title = "";
        content = "";
        audioUrls.clear();
        screensUrls.clear();
    }
}
