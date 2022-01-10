/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.tealc.streams;

public class TopologyProducerConfig {
    private final String sourceTopic;
    private final String targetTopic;

    public TopologyProducerConfig(String sourceTopic, String targetTopic) {
        this.sourceTopic = sourceTopic;
        this.targetTopic = targetTopic;
    }

    public static TopologyProducerConfig fromEnv() {
        String sourceTopic = System.getenv("SOURCE_TOPIC");
        String targetTopic = System.getenv("TARGET_TOPIC");

        return new TopologyProducerConfig(sourceTopic, targetTopic);
    }

    public String getSourceTopic() {
        return sourceTopic;
    }

    public String getTargetTopic() {
        return targetTopic;
    }

    @Override
    public String toString() {
        return "TopologyProducerConfig{" +
                ", sourceTopic='" + sourceTopic + '\'' +
                ", targetTopic='" + targetTopic + '\'' +
                '}';
    }
}
