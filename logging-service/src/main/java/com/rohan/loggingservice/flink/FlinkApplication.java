package com.rohan.loggingservice.flink;

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

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers(bootstrapServer)
                .setTopics(inputTopic)
                .setGroupId("flinkService")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.builder()
                .setValueSerializationSchema(new SimpleStringSchema())
                .setTopic(outputTopic)
                .build();

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers(bootstrapServer)
                .setRecordSerializer(serializer)
                .build();

        DataStream<String> stream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source")
                .map(orderNumber -> orderNumber + " PROCESSING")
                .setParallelism(1);

        stream.sinkTo(kafkaSink);

        env.execute(jobTitle);
    }
}
