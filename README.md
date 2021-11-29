# Run Locally
```bash
$ gradle clean execute \
    -DmainClass=com.auddia.PubSubToPubSub \
    -Dexec.args="--inputProject=vodacast-staging \
                 --outputProject=vodacast-staging \
                 --topicListLocation=sample.txt" \
    -Pdirect-runner
```

# Compile Template
```bash
$ gradle clean execute \
    -DmainClass=com.auddia.PubSubToPubSub \
    -Dexec.args="--runner=DataflowRunner \
                 --project=vodacast-staging \
                 --topicMapLocation=test_topic_list \
                 --inputProject=vodacast-staging \
                 --outputProject=vodacast-staging \
                 --templateLocation=gs://vodacast-staging-events/dataflow/templates/sink_template_test \
                 --region=us-central1 \
                 --gcpTempLocation=gs://vodacast-staging-events/dataflow/temp"
```