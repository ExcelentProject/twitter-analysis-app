FROM quay.io/strimzi/kafka:latest-kafka-3.0.0
USER root:root
# RUN mkdir -p /opt/kafka/plugins/camel
COPY connectors/ /opt/kafka/plugins/
USER 1001
