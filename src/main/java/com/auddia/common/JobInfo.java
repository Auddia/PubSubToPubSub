package com.auddia.common;

public class JobInfo {
    private final String topic;
    private final String inputProject;
    private final String outputProject;

    public JobInfo(String topic, String inputProject, String outputProject) {
        this.topic = topic;
        this.inputProject = inputProject;
        this.outputProject = outputProject;
    }

    public String getSubscription() {
        return String.format("%s/%s.input", inputProject, topic);
//        return String.format("projects/%s/subscriptions/%s_%s.sink", inputProject, topic, outputProject);
    }

    public String getTopic() {
        return String.format("%s/%s/%s.out", inputProject, outputProject, topic);
//        return String.format("projects/%s/topics/%s", outputProject, topic);
    }
}
