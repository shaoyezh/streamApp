package streams;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.processor.api.Processor;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

public class StreamApp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streamsApp");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        IntegerSerializer sinkValueSerializer = new IntegerSerializer();
        System.out.println("Start");
        Topology builder = new Topology();
    // add the source processor node that takes Kafka "source-topic" as input
        ProcessorSupplier<String, String, String, Integer> ps = new ProcessorSupplier<String, String, String, Integer>() {
            @Override
            public Processor<String, String, String, Integer> get() {
                return new MLProcessor();
            }

            public Set<StoreBuilder<?>> stores() {
                final StoreBuilder<KeyValueStore<String, Integer>> countsStoreBuilder =
                        Stores
                                .keyValueStoreBuilder(
                                        Stores.persistentKeyValueStore("Counts"),
                                        Serdes.String(),
                                        Serdes.Integer()
                                );
                return Collections.singleton(countsStoreBuilder);
            }
        };
        builder.addSource("Source", "streamApp-input");
                    // add the WordCountProcessor node which takes the source processor as its upstream processor.
                    // the ProcessorSupplier provides the count store associated with the WordCountProcessor
        builder.addProcessor("Process", ps,"Source");
                    // add the sink processor node that takes Kafka topic "sink-topic" as output
                    // and the WordCountProcessor node as its upstream processor
        builder.addSink("Sink", "stream-app-output", null, sinkValueSerializer,"Process");

        final KafkaStreams streams = new KafkaStreams(builder, props);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                System.out.println("stream closed");
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            System.out.println("Stream start");
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}
