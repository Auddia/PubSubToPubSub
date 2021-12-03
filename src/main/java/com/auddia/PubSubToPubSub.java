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
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class PubSubToPubSub {
    public static Map<String, String> getTopicMap(String project, String bucket, String topicMapLocation) {
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(project)
                .build()
                .getService();

        Blob blob = storage.get(bucket, topicMapLocation);
        String content = new String(blob.getContent());

        return Arrays.stream(content.split("[\\r\\n]+"))
                .map(line -> line.split(","))
                .collect(Collectors.toMap(list -> list[0], list -> list[1]));
    }

    public static List<JobInfo> getJobInfo(String inputProject, String outputProject, String topicMapLocation) {
        Map<String, String> topicMap = getTopicMap(
                inputProject,
                String.format("%s-events", inputProject),
                String.format("configurations/sink/%s", topicMapLocation)
        );

        return topicMap.keySet().stream()
                .map(inputTopic -> new JobInfo(inputTopic, topicMap.get(inputTopic), inputProject, outputProject))
                .collect(Collectors.toList());
    }

    static void runPubSubToPubSub(PubSubToPubSubOptions options) {
        Pipeline pipeline = Pipeline.create(options);

        List<JobInfo> jobs = getJobInfo(
                options.as(DataflowPipelineOptions.class).getProject(),
                options.getOutputProject().get(),
                options.getTopicMapLocation().get()
        );

        PCollectionList<PubsubMessage> messages = PCollectionList.of(
                jobs.stream()
                    .map(jobInfo -> pipeline.apply(
                            String.format("GetMessagesFromSubscription%s", jobInfo.getSubscriptionName()),
                            PubsubIO.readMessages().fromSubscription(jobInfo.getSubscription())
                        )
                    )
                    .collect(Collectors.toList())
        );

        for (int idx = 0; idx < jobs.size(); idx++) {
            JobInfo info = jobs.get(idx);
            PCollection<PubsubMessage> message = messages.get(idx);

            message.apply(
                    String.format("SinkMessagesToTopic%s", info.getOutputTopicName()),
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
