package com.mooduplabs.debuggit;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class Report {

    private static final String STEPS_TO_REPRODUCE_TITLE = "%1$sSteps to reproduce%1$s: ";
    private static final String ACTUAL_BEHAVIOUR_TITLE = "%1$sActual behaviour%1$s: ";
    private static final String EXPECTED_BEHAVIOUR_TITLE = "%1$sExpected behaviour%1$s: ";
    private static final String END_LINE = "\n\n";

    private String title;
    private String kind;
    private String priority;
    private String stepsToReproduce;
    private String actualBehaviour;
    private String expectedBehaviour;
    private List<String> audioUrls;
    private List<String> screensUrls;

    protected Report() {
        this.audioUrls = new ArrayList<>();
        this.screensUrls = new ArrayList<>();
    }

    protected String getTitle() {
        return title == null ? "" : title;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    protected List<String> getAudioUrls() {
        return audioUrls;
    }

    protected String getKind() {
        return kind == null ? "" : kind;
    }

    protected void setKind(String kind) {
        this.kind = kind;
    }

    protected String getPriority() {
        return priority == null ? "" : priority;
    }

    protected void setPriority(String priority) {
        this.priority = priority;
    }

    protected String getStepsToReproduce() {
        return stepsToReproduce == null ? "" : stepsToReproduce;
    }

    protected void setStepsToReproduce(String stepsToReproduce) {
        this.stepsToReproduce = stepsToReproduce;
    }

    protected String getActualBehaviour() {
        return actualBehaviour == null ? "" : actualBehaviour;
    }

    protected void setActualBehaviour(String actualBehaviour) {
        this.actualBehaviour = actualBehaviour;
    }

    protected String getExpectedBehaviour() {
        return expectedBehaviour == null ? "" : expectedBehaviour;
    }

    protected void setExpectedBehaviour(String expectedBehaviour) {
        this.expectedBehaviour = expectedBehaviour;
    }

    protected List<String> getScreensUrls() {
        return screensUrls;
    }

    protected void clear() {
        title = "";
        kind = "";
        priority = "";
        stepsToReproduce = "";
        actualBehaviour = "";
        expectedBehaviour = "";
        audioUrls.clear();
        screensUrls.clear();
    }

    protected String getContent(Activity activity) {
        String boldMark;
        switch(DebuggIt.getInstance().getConfigType()) {
            case JIRA:
                boldMark = "*";
                break;
            case GITHUB:
            case BITBUCKET:
            default:
                boldMark = "**";

        }
        return String.format(STEPS_TO_REPRODUCE_TITLE, boldMark) + getStepsToReproduce() + END_LINE +
                String.format(ACTUAL_BEHAVIOUR_TITLE, boldMark) + getActualBehaviour() + END_LINE +
                String.format(EXPECTED_BEHAVIOUR_TITLE, boldMark) + getExpectedBehaviour() + END_LINE
                + Utils.getUrlAsStrings(screensUrls, false)
                + Utils.getUrlAsStrings(audioUrls, true)
                + Utils.getDeviceInfoString(activity);
    }
}
