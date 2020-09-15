FROM strimzi/kafka:latest-kafka-2.6.0
USER root:root
# RUN mkdir -p /opt/kafka/plugins/camel
COPY connectors/ /opt/kafka/plugins/
USER 1001
