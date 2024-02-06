package com.rohan.loggingservice.flink;

import com.rohan.loggingservice.OrderEvent;
import com.rohan.loggingservice.flink.serialization.OrderEventDeserializer;
import com.rohan.loggingservice.flink.serialization.OrderEventSerializer;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
public class FlinkApplication {

    final static String inputTopic = "flinkTopic";
    final static String outputTopic = "flinkOutput";
    final static String jobTitle = "id-processing";

    public static void main(String[] args) throws Exception{

        final String bootstrapServer = "localhost:9092";

        Configuration configuration = new Configuration();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

        KafkaSource<OrderEvent> kafkaSource = KafkaSource.<OrderEvent>builder()
                .setBootstrapServers(bootstrapServer)
                .setTopics(inputTopic)
                .setGroupId("flinkService")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new OrderEventDeserializer())
                .build();

        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.builder()
                .setValueSerializationSchema(new SimpleStringSchema())
                .setTopic(outputTopic)
                .build();

        KafkaSink<OrderEvent> kafkaSink = KafkaSink.<OrderEvent>builder()
                .setBootstrapServers(bootstrapServer)
                .setRecordSerializer(new OrderEventSerializer("flinkOutput"))
                .build();

        DataStream<OrderEvent> stream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        DataStream<String> streamProcessingFunction = stream
                .map(orderNumber -> orderNumber + " PROCESSING")
                .setParallelism(1);

        streamProcessingFunction.print();

        stream.sinkTo(kafkaSink);

        env.execute(jobTitle);
    }
}
