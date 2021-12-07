#!/usr/bin/env bash
#######################################################################################################################
# This shell script compiles the Dataflow template for the specified project
#
# Script Args:
#   input_project: The GCP project
#   output_project: The GCP project
#   pipeline: The pipeline name
#   version: The version of the template (Optional)
#
#######################################################################################################################
BASE_DIR="$(cd "$(dirname "$0")" || exit; pwd -P)"
pushd "$BASE_DIR" || exit

# Default variable definitions
VERSION="beta"


# Get the arguments
for ARGUMENT in "$@"
do
    KEY=$(echo "$ARGUMENT" | cut -f1 -d=)
    VALUE=$(echo "$ARGUMENT" | cut -f2 -d=)
    case "$KEY" in
            input_project)        INPUT_PROJECT=${VALUE} ;;
            output_project)       OUTPUT_PROJECT=${VALUE} ;;
            pipeline)             PIPELINE=${VALUE} ;;
            version)              VERSION=${VALUE} ;;
            *)
    esac
done

if [ -z "$INPUT_PROJECT" ] || [ -z "$OUTPUT_PROJECT" ] || [ -z "$PIPELINE" ]
then
    echo "Missing one or more of the required arguments input_project=<input_project>, output_project=<output_project>, pipeline=<pipeline>"
    exit
fi


# Source the common variables from the bin
TEMPLATE_NAME=$PIPELINE"_to_"$OUTPUT_PROJECT"_topic_sink_version_"$VERSION$(date +"-%Y-%m-%d")
BUCKET=$INPUT_PROJECT"-events"
NAME=$(echo $PIPELINE | tr "_" -)

if [ $INPUT_PROJECT = "cfr" ]
then
    INPUT_PROJECT="cfr-projects"
fi

if [ $OUTPUT_PROJECT = "cfr" ]
then
    OUTPUT_PROJECT="cfr-projects"
fi


gradle clean execute \
    -DmainClass=com.auddia.PubSubToPubSub \
    -Dexec.args="--runner=DataflowRunner \
                 --region=us-central1 \
                 --jobName=topic-sink-${NAME}
                 --templateLocation=gs://$BUCKET/dataflow/templates/$TEMPLATE_NAME \
                 --gcpTempLocation=gs://$BUCKET/dataflow/temp \
                 --stagingLocation=gs://$BUCKET/dataflow/staging/ \
                 --project=$INPUT_PROJECT \
                 --outputProject=$OUTPUT_PROJECT \
                 --topicMapLocation=$PIPELINE.txt"

popd || exit

