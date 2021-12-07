# Run Locally
```bash
$ gradle clean execute \
    -DmainClass=com.auddia.PubSubToPubSub \
    -Dexec.args="--inputProject=vodacast-staging \
                 --project=vodacast-staging \
                 --outputProject=vodacast-staging \
                 --topicListLocation=sample.txt" \
    -Pdirect-runner
```

# Compile Template Examples

Templates for vodacast to vodacast-staging sink
```bash
$ ./compile_template.sh input_project=vodacast output_project=vodacast-staging pipeline=api_domain_events version=<VERSION_TAG>
$ ./compile_template.sh input_project=vodacast output_project=vodacast-staging pipeline=device_events version=<VERSION_TAG>
$ ./compile_template.sh input_project=vodacast output_project=vodacast-staging pipeline=discovery_api_domain_events version=<VERSION_TAG>
$ ./compile_template.sh input_project=vodacast output_project=vodacast-staging pipeline=hub_domain_events version=<VERSION_TAG>
```