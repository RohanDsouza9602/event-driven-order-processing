package com.rohan.flinkservice.flink;

import com.rohan.flinkservice.flink.event.OrderEvent;
import com.rohan.flinkservice.flink.serialization.OrderEventDeserializer;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;


import java.math.BigDecimal;
public class FlinkOnlyRevenue {

    final static String inputTopic = "notificationId";
    final static String outputTopic = "flinkOutput";
    final static String jobTitle = "revenue-processing";
    final static ObjectMapper objectMapper = new ObjectMapper();


    public static void main(String[] args) throws Exception {


        final String bootstrapServer = "localhost:9092";

        Configuration configuration = new Configuration();

        int defaultLocalParallelism = Runtime.getRuntime().availableProcessors();
        configuration.setString("taskmanager.memory.network.max", "2gb");

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

//        env.enableCheckpointing(5000);
//        env.setStateBackend(new EmbeddedRocksDBStateBackend());

        KafkaSource<OrderEvent> kafkaSource = KafkaSource.<OrderEvent>builder()
                .setBootstrapServers(bootstrapServer)
                .setTopics(inputTopic)
                .setGroupId("flinkStatus")
                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST))
                .setValueOnlyDeserializer(new OrderEventDeserializer())
                .setProperty("enable.auto.commit", "true")  // Enable automatic offset committing
                .setProperty("auto.commit.interval.ms", "1000")  // Set the interval for automatic offset committing (in milliseconds)
//                .setProperty("isolation.level", "read_committed")
                .build();

        DataStream<OrderEvent> stream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // KeySelector for keying according to orderStatus
        KeySelector<OrderEvent, String> numberKeySelector = new KeySelector<OrderEvent, String>() {
            @Override
            public String getKey(OrderEvent orderEvent) throws Exception {
                return orderEvent.getOrderNumber();
            }
        };

        KeyedStream<OrderEvent, String> keyedStream = stream.keyBy(numberKeySelector);

        DataStream<BigDecimal> resultTotalRevenue = keyedStream
                .map(new MapFunction<OrderEvent, BigDecimal>() {
                    @Override
                    public BigDecimal map(OrderEvent orderEvent) throws Exception {
                        return orderEvent.getTotalOrderValue();
                    }
                })
                .keyBy(new KeySelector<BigDecimal, String>() {
                    @Override
                    public String getKey(BigDecimal orderValue) throws Exception {
                        return "totalRevenue"; // Fixed key for total revenue
                    }
                })
                .map(new RichMapFunction<BigDecimal, BigDecimal>() {
                    // Declare the state variable
                    private transient ValueState<BigDecimal> totalRevenueState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        // Initialize the state variable
                        totalRevenueState = getRuntimeContext().getState(new ValueStateDescriptor<>("totalRevenue", BigDecimal.class));
                    }

                    @Override
                    public BigDecimal map(BigDecimal orderValue) throws Exception {
                        // Access the total revenue state
                        BigDecimal currentTotalRevenue = totalRevenueState.value();
                        // Calculate new total revenue by adding the current order value
                        currentTotalRevenue = (currentTotalRevenue != null) ? currentTotalRevenue.add(orderValue) : orderValue;
                        // Update the state with the new total revenue
                        totalRevenueState.update(currentTotalRevenue);
                        // Return the updated total revenue
                        return currentTotalRevenue;
                    }
                })
                .setParallelism(400);;


        DataStream<BigDecimal> windowRevenue = keyedStream
                .map(new MapFunction<OrderEvent, BigDecimal>() {
                    @Override
                    public BigDecimal map(OrderEvent orderEvent) throws Exception {
                        return orderEvent.getTotalOrderValue();
                    }
                })
                .keyBy(new KeySelector<BigDecimal, String>() {
                    @Override
                    public String getKey(BigDecimal orderValue) throws Exception {
                        return "totalRevenue"; // Fixed key for total revenue
                    }
                })
                .window(TumblingProcessingTimeWindows.of(Time.seconds(30))) // Define tumbling processing time windows of 30 seconds
                .reduce(new ReduceFunction<BigDecimal>() {
                    @Override
                    public BigDecimal reduce(BigDecimal value1, BigDecimal value2) throws Exception {
                        return value1.add(value2); // Sum the values within each window
                    }
                })
                .setParallelism(400);

        resultTotalRevenue.print("Total Revenue =").setParallelism(400);
        windowRevenue.print("Total Revenue in the last 30 seconds =").setParallelism(400);

        env.execute(jobTitle);
    }
}
