package com.mooduplabs.debuggit;

import java.util.ArrayList;
import java.util.List;

public class Report {

    private static final String STEPS_TO_REPRODUCE_TITLE = "**Steps to reproduce**: ";
    private static final String ACTUAL_BEHAVIOUR_TITLE = "**Actual behaviour**: ";
    private static final String EXPECTED_BEHAVIOUR_TITLE = "**Expected behaviour**: ";
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

    protected String getContent() {
        return STEPS_TO_REPRODUCE_TITLE + getStepsToReproduce() + END_LINE +
                ACTUAL_BEHAVIOUR_TITLE + getActualBehaviour() + END_LINE +
                EXPECTED_BEHAVIOUR_TITLE + getExpectedBehaviour() + END_LINE;
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
        kind = "";
        priority = "";
        stepsToReproduce = "";
        actualBehaviour = "";
        expectedBehaviour = "";
        audioUrls.clear();
        screensUrls.clear();
    }
}
