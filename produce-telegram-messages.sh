#!/bin/bash
set -v

TOPIC=${1}

kubectl run kafka-producer -ti --image=strimzi/kafka:latest-kafka-2.6.0 --rm=true --restart=Never -- bin/kafka-console-producer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic ${TOPIC}
