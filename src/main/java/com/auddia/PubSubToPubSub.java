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
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
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
    public static List<String> getTopicList(String topicListLocation) {
        return Arrays.asList("topic_one",  "topic_two");
    }

    public static List<JobInfo> getJobInfo(PubSubToPubSubOptions options) {
        String outputProject = options.getOutputProject().get();
        List<String> topics = getTopicList(options.getTopicListLocation().get());

        return topics.stream()
                .map(topic -> new JobInfo(topic, "test_data", outputProject))
                .collect(Collectors.toList());
    }

    static void runPubSubToPubSub(PubSubToPubSubOptions options) {
        Pipeline pipeline = Pipeline.create(options);

        List<JobInfo> jobs = getJobInfo(options);

        // TODO: Validate ordering is consistent with topics array (if not make a PCollection and partition the data)
        PCollectionList<String> messages = PCollectionList.of(
                jobs.stream()
                        .map(jobInfo -> pipeline.apply(
                                "GetData", TextIO.read().from(jobInfo.getSubscription())
                        ))
                        .collect(Collectors.toList())
        );

        for (int idx = 0; idx < jobs.size(); idx++) {
            JobInfo info = jobs.get(idx);
            PCollection<String> message = messages.get(idx);

            message.apply(
                    "SinkMessages",
                    TextIO.write().to(info.getTopic())
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
