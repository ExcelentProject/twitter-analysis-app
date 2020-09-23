#!/bin/bash
set -v

TOPIC=${1}

kubectl run kafka-consumer -ti --image=strimzi/kafka:0.19.0-kafka-2.5.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic ${TOPIC} --from-beginning
