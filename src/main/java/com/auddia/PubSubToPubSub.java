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

import com.auddia.common.PubSubToPubSubOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.apache.beam.sdk.values.TypeDescriptors;

/**
 *
 */
public class PubSubToPubSub {
    public static class ProcessTopics extends PTransform<PCollection<String>, PCollection<KV<String, String>>> {
        private String getOutputLocation(String project, String topic) {
            return String.format("projects/%s/topics/%s", project, topic);
        }

        private String getInput(String inputProject, String outputProject, String topic) {
            return String.format(
                    "projects/%s/subscriptions/%s_%s.sink",
                    inputProject,
                    topic,
                    outputProject
            );
        }

        @Override
        public PCollection<KV<String, String>> expand(PCollection<String> topics) {
            return topics.apply(
                    MapElements
                            .into(TypeDescriptors.kvs(TypeDescriptors.strings(), TypeDescriptors.strings()))
                            .via((String topic) -> KV.of(
                                    this.getOutputLocation("input", topic),
                                    this.getInput("input", "output", topic)
                            ))
            );
        }
    }

    public static class FormatAsTextFn extends SimpleFunction<KV<String, String>, String> {
        @Override
        public String apply(KV<String, String> input) {
            return input.getKey() + ": " + input.getValue();
        }
    }

    static void runPubSubToPubSub(PubSubToPubSubOptions options) {
        Pipeline pipeline = Pipeline.create(options);

        pipeline
            .apply("GetTopicList", TextIO.read().from(options.getTopicListLocation()))
            .apply("ProcessTopics", new ProcessTopics())
            .apply(MapElements.via(new PubSubToPubSub.FormatAsTextFn()))
            .apply("SinkData", TextIO.write().to("test_sink"));

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
