/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KafkaStreamsExample {
    private static final Logger log = LogManager.getLogger(KafkaStreamsExample.class);

    public static String getMessageText(String message) {
        Pattern r = Pattern.compile("text='([^']+)'");
        Matcher m = r.matcher(message);
        if (m.find()) {
            return m.group(1);
        } else {
            return "Cannot parse the message!";
        }
    }

    public static String getUser(String message) {
        Pattern r = Pattern.compile("firstName='([^']+)',");
        Matcher m = r.matcher(message);
        String firstName = "";
        String lastName = "";
        if (m.find()) {
            firstName = m.group(1);
        } else {
            firstName = "John";
        }

        r = Pattern.compile("lastName='([^']+)',");
        m = r.matcher(message);
        if (m.find()) {
            lastName = m.group(1);
        } else {
            lastName = "Doe";
        }

        return firstName + " " + lastName;
    }


    public static void main(String[] args) {
        KafkaStreamsConfig config = KafkaStreamsConfig.fromEnv();

        log.info(KafkaStreamsConfig.class.getName() + ": {}",  config.toString());

        Properties props = KafkaStreamsConfig.createProperties(config);

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(config.getSourceTopic(), Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues(value -> {
                    String message = getMessageText(value);
                    log.info("Message: " + message);
                    if (message.toLowerCase().contains("ahoj") ||
                            message.toLowerCase().contains("hello") ||
                            message.toLowerCase().contains("hi") ||
                            message.toLowerCase().contains("cau")) {
                        log.info("Response: Hello " + getUser(value) + "! How are you?");
                        return "Hello " + getUser(value) + "! How are you?";
                    } else if (message.toLowerCase().contains("your name")) {
                        log.info("Response: My name is strimzi-connect-bot");
                        return "My name is strimzi-connect-bot";
                    } else {
                        log.info("Response: Thank you for contacting me! However, I don't know what should I response to your message: " + message);
                        return "Thank you for contacting me! However, I don't know what should I response to your message: " + message;
                    }
                })
                .transformValues(KafkaStreamsHeaderTransformer::new)
                .to(config.getTargetTopic(), Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams;

        streams = new KafkaStreams(builder.build(), props);

        streams.start();
    }
}
