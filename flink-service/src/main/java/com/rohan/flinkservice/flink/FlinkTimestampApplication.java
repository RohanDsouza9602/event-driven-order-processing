package com.rohan.flinkservice.flink;

import com.rohan.flinkservice.flink.event.OrderEvent;
import com.rohan.flinkservice.flink.event.TimedOrderEvent;
import com.rohan.flinkservice.flink.serialization.OrderEventDeserializer;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class   FlinkTimestampApplication {

    final static String inputTopic = "notificationId";
    final static String outputTopic = "flinkTimeOutput";
    final static String jobTitle = "timestamp-processing";

    public static void main(String[] args) throws Exception{

        final String bootstrapServer = "localhost:9092";

        Configuration configuration = new Configuration();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(configuration);

//        env.enableCheckpointing(5000);
//        env.setStateBackend(new EmbeddedRocksDBStateBackend());

        KafkaSource<OrderEvent> kafkaSource = KafkaSource.<OrderEvent>builder()
                .setBootstrapServers(bootstrapServer)
                .setTopics(inputTopic)
                .setGroupId("flinkTimeService")
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .setValueOnlyDeserializer(new OrderEventDeserializer())
                .setProperty("enable.auto.commit", "true")
                .setProperty("auto.commit.interval.ms", "1000")
//                .setProperty("isolation.level", "read_committed")
                .build();

//        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.builder()
//                .setValueSerializationSchema(new SimpleStringSchema())
//                .setTopic(outputTopic)
//                .build();

//        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
//                .setBootstrapServers(bootstrapServer)
////                .setRecordSerializer(new OrderEventSerializer("flinkOutput"))
//                .setRecordSerializer(serializer)
////                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
//                .build();

        DataStream<OrderEvent> stream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        DataStream<TimedOrderEvent> timedOrderEventStream = stream
                .map(new MapFunction<OrderEvent, TimedOrderEvent>() {
                    @Override
                    public TimedOrderEvent map(OrderEvent orderEvent) throws Exception {
                        Long currentTimestamp = System.currentTimeMillis();
                        LocalDateTime localDateTime;
                        Instant instant = Instant.ofEpochMilli(currentTimestamp);
                        Duration duration = Duration.ofHours(5).plusMinutes(30);
                        return new TimedOrderEvent(orderEvent, currentTimestamp, instant.plus(duration));
                    }
                });

        timedOrderEventStream.print().setParallelism(1);


        env.execute(jobTitle);

    }

}
