package com.moodup.bugreporter;

import android.os.Build;

import com.jaredrummler.android.device.DeviceName;

import java.util.ArrayList;
import java.util.List;

public class Report {

    private String title;
    private String kind;
    private String priority;
    private String stepsToReproduce;
    private String actualBehaviour;
    private String expectedBehaviour;
    private String applicationVersion;
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
        return "**Device**: " + DeviceName.getDeviceName() + "\n\n" +
                String.format("**Android version**: %s (API %d)\n\n", Build.VERSION.RELEASE, Build.VERSION.SDK_INT) +
                "**Application version**: " + getApplicationVersion() + "\n\n" +
                "**Steps to reproduce**: " + getStepsToReproduce() + "\n\n" +
                "**Actual behaviour**: " + getActualBehaviour() + "\n\n" +
                "**Expected behaviour**: " + getExpectedBehaviour() + "\n\n";
    }

    protected List<String> getAudioUrls() {
        return audioUrls;
    }

    public String getKind() {
        return kind == null ? "" : kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getPriority() {
        return priority == null ? "" : priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStepsToReproduce() {
        return stepsToReproduce == null ? "" : stepsToReproduce;
    }

    public void setStepsToReproduce(String stepsToReproduce) {
        this.stepsToReproduce = stepsToReproduce;
    }

    public String getActualBehaviour() {
        return actualBehaviour == null ? "" : actualBehaviour;
    }

    public void setActualBehaviour(String actualBehaviour) {
        this.actualBehaviour = actualBehaviour;
    }

    public String getExpectedBehaviour() {
        return expectedBehaviour == null ? "" : expectedBehaviour;
    }

    public void setExpectedBehaviour(String expectedBehaviour) {
        this.expectedBehaviour = expectedBehaviour;
    }

    public String getApplicationVersion() {
        return applicationVersion == null ? "" : applicationVersion;
    }

    public void setApplicationVersion(String version) {
        this.applicationVersion = version;
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
        stepsToReproduce = "";
        actualBehaviour = "";
        expectedBehaviour = "";
        audioUrls.clear();
        screensUrls.clear();
    }
}
