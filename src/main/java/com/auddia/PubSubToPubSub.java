/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.auddia;

import com.auddia.common.JobInfo;
import com.auddia.common.PubSubToPubSubOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class PubSubToPubSub {
    public static List<String> getTopicList(String project, String bucket, String topicListLocation) {
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(project)
                .build()
                .getService();

        Blob blob = storage.get(bucket, topicListLocation);
        String content = new String(blob.getContent());

        return Arrays.asList(content.split("[\\r\\n]+"));
    }

    public static List<JobInfo> getJobInfo(PubSubToPubSubOptions options) {
        String inputProject = options.as(DataflowPipelineOptions.class).getProject();
        String outputProject = options.getOutputProject().get();
        List<String> topics = getTopicList(
                inputProject,
                String.format("%s-sink", inputProject),
                options.getTopicListLocation().get()
        );

        return topics.stream()
                .map(topic -> new JobInfo(topic, inputProject, outputProject))
                .collect(Collectors.toList());
    }

    static void runPubSubToPubSub(PubSubToPubSubOptions options) {
        Pipeline pipeline = Pipeline.create(options);

        List<JobInfo> jobs = getJobInfo(options);

        PCollectionList<PubsubMessage> messages = PCollectionList.of(
                jobs.stream()
                    .map(jobInfo -> pipeline.apply(
                            "GetMessages",
                            PubsubIO.readMessages().fromSubscription(jobInfo.getSubscription())
                        )
                    )
                    .collect(Collectors.toList())
        );

        for (int idx = 0; idx < jobs.size(); idx++) {
            JobInfo info = jobs.get(idx);
            PCollection<PubsubMessage> message = messages.get(idx);

            message.apply(
                    "SinkMessages",
                    PubsubIO.writeMessages().to(info.getTopic())
            );
        }

        pipeline.run();
    }

    public static void main(String[] args) {
        PubSubToPubSubOptions options = PipelineOptionsFactory
                .fromArgs(args)
                .withValidation()
                .as(PubSubToPubSubOptions.class);

        runPubSubToPubSub(options);
    }
}
