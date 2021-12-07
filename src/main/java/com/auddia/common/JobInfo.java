package com.auddia.common;

public class JobInfo {
    private final String inputTopic;
    private final String outputTopic;
    private final String inputProject;
    private final String outputProject;

    public JobInfo(String inputTopic, String outputTopic, String inputProject, String outputProject) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.inputProject = inputProject;
        this.outputProject = outputProject;
    }

    public String getSubscriptionName() {
        return String.format("%s_%s.df_sink", inputTopic, outputProject);
    }

    public String getOutputTopicName(){
        return outputTopic;
    }

    public String getSubscription() {
        return String.format("projects/%s/subscriptions/%s.%s_subscription", inputProject, inputTopic, outputProject);
    }

    public String getTopic() {
        return String.format("projects/%s/topics/%s", outputProject, outputTopic);
    }
}
