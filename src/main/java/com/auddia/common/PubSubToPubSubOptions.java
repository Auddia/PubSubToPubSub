package com.auddia.common;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.options.ValueProvider;

public interface PubSubToPubSubOptions extends PipelineOptions {
    @Description("The project that the topics are being read from")
    @Validation.Required
    ValueProvider<String> getInputProject();
    void setInputProject(ValueProvider<String> value);

    @Description("The project that the topics are being outputted to")
    @Validation.Required
    ValueProvider<String> getOutputProject();
    void setOutputProject(ValueProvider<String> value);

    @Description("The GCS location of the topic list")
    @Validation.Required
    ValueProvider<String> getTopicListLocation();
    void setTopicListLocation(ValueProvider<String> value);
}
