```bash
$ gradle clean execute \
    -DmainClass=com.auddia.PubSubToPubSub \
    -Dexec.args="--inputProject=vodacast-staging \
                 --outputProject=vodacast-staging \
                 --topicListLocation=sample.txt" \
    -Pdirect-runner
```