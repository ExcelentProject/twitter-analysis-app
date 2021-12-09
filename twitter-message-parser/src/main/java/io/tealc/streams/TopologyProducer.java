package io.tealc.streams;

import io.tealc.model.TweetSerde;
import io.tealc.model.twitter.HashtagEntity;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TopologyProducer {
    private static final Logger LOG = Logger.getLogger(TopologyProducer.class);

    TopologyProducerConfig topologyProducerConfig = TopologyProducerConfig.fromEnv();

    @Produces
    public Topology buildTopology() {
        final TweetSerde tweetSerde = new TweetSerde();
        final StreamsBuilder builder = new StreamsBuilder();

        builder.stream(topologyProducerConfig.getSourceTopic(), Consumed.with(Serdes.ByteArray(), tweetSerde))
                .flatMapValues(value -> {
                    if (value.getRetweetedStatus() != null)  {
                        // We ignore retweets => we do not want alert for every retweet
                        return List.of();
                    } else {
                        String tweetText = value.getText();
                        String tweetAuthor = value.getUser().getScreenName();
                        String tweetHashtags = Arrays.stream(value.getHashtagEntities()).map(HashtagEntity::getText).collect(Collectors.joining(","));
                        String statusUrl = "https://twitter.com/" + value.getUser().getScreenName() + "/status/" + value.getId();

                        String telegramTweet = String.format("Hey! There is a new tweet which might interest you!\n" +
                                        "*Hashtags*: '%s'\n" +
                                        "*Author*: %s\n" +
                                        "*URL*: %s\n\n" +
                                        "%s\n",
                                tweetHashtags, tweetAuthor, statusUrl, tweetText);

                        return List.of(telegramTweet);
                    }
                })
                .peek((key, value) -> {
                    LOG.infov("Parsed new tweet:\n{0}", value);
                })
                .to(topologyProducerConfig.getTargetTopic(), Produced.with(Serdes.ByteArray(), Serdes.String()));

        return builder.build();
    }
}
