 /*
  * Copyright Strimzi authors.
  * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
  */


 import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
 import org.apache.kafka.streams.processor.ProcessorContext;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;

 public class KafkaStreamsHeaderTransformer implements ValueTransformerWithKey<String, String, String> {
     private static final Logger log = LogManager.getLogger(KafkaStreamsHeaderTransformer.class);

    ProcessorContext context;

    @Override
    public void init(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public String transform(String readOnlyKey, String value) {
        // This is needed inc ase you are using non-patched camel-telegram plugin
        // context.headers().remove("CamelHeader.CamelTelegramChatId");
        // context.headers().remove("CamelProperty.CamelToEndpoint");
        log.info(context.headers());
        return value;
    }

    @Override
    public void close() {

    }
}
