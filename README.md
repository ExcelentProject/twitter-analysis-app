# Strimzi from QE POV

This demo is for webinar about Strimzi/AMQ Streams from QE point of view.
Demo is based on different demo created by Jakub Scholz [Metamorphosis: When Kafka Meets Camel](https://github.com/scholzj/devconf-2020-metamorphosis-when-kafka-meets-camel/).

## Prerequisites

Before we start, we should install the prerequisites which will be used later:
* Secret with Telegram credentials (not part of this repository because they are secret ;-))
* Strimzi cluster operator

```bash
oc apply -f 00-telegram-credentials.yaml
oc apply -f install/
```

## Secrets with credentials

The properties file for Telegram should look like this:

```properties
token=123:xxx
```

The resulting Kubernetes secret should look like this:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: telegram-credentials
type: Opaque
data:
  telegram-credentials.properties: <Base64 of the properties file>
```

## Kafka cluster

Next we have to deploy the Kafka cluster.
It is mostly standard Kafka cluster with Kafka 2.6.0

```bash
oc apply -f 01-kafka.yaml
```

## Deploy Kafka Connect

Next we need to deploy the Kafka Connect cluster.
It uses container image which was pre-built with the Camel Kafka connectors.
This image also contains fix for bug inside camel-telegram. [issue-link](issue-link)

```bash
oc apply -f 02-kafka-connect.yaml
```

In the Kafka Connect, notice the:
* Secret Telegram credentials mounted into the Connect cluster

```yaml
  externalConfiguration:
    volumes:
      - name: telegram-credentials
        secret:
          secretName: telegram-credentials
```

* The configuration provider

```yaml
  config:
    config.providers: file
    config.providers.file.class: org.apache.kafka.common.config.provider.FileConfigProvider
```

This also creates topics and users used by the Kafka Connect.
One the Kafka Connect is deployed, you can check the available connectors in its status.
You should see `CamelTelegramSourceConnector` and `CamelTelegramSinkConnector` among them.

## Deploy the Telegram connectors and KafkaStreams app

The Telegram source connector can be deployed using the KafkaConnector custom resource.

```bash
oc apply -f 03-telegram-connector-source.yaml
```

In the YAML, notice how the API token is mounted from a secret.
Once the connector is deployed, you can check the status to see if it is running, you can go to the Telegram app and talk with the bot `@strimzi-connect-bot`.
Run receiver on a Kafka topic `telegram-topic-source` to see the messages sent to the bot.

```bash
kubectl run kafka-consumer -ti --image=strimzi/kafka:0.19.0-kafka-2.6.0 --rm=true --restart=Never -- bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic telegram-topic-source --from-beginning
```

or use

```bash
./get-telegram-messages.sh telegram-topic-source
```

For demo of Telegram sink connector we need to deploy our KafkaStreams app. You can do it by the following command:
```bash
oc apply -f clients-examples/hello-world-streams.yaml
```

The Telegram sink connector can be deployed using the KafkaConnector custom resource.

```bash
oc apply -f 04-telegram-connector-sink.yaml
```

In the YAML, notice how the API token is mounted from a secret.
Once the connector is deployed, you can check the status to see if it is running, you can go to the Telegram app and talk with the bot `@StrimziQEBot`.
The bot will response on every your message.
It's achieved by simple KafkaStreams application which reads messages from `telegram-topic-source` and based on data there send response to `telegram-topic-sink`.
Telegram sink connector then send messages from `telegram-topic-sink` to specific chat based on telegram `chatId`.

Kafka Streams app example could be found in [clients-examples](clients-examples/hello-world-streams.yaml).
