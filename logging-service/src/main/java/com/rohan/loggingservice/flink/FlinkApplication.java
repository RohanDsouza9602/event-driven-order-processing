package com.rohan.loggingservice.flink;

import com.rohan.loggingservice.OrderEvent;
import com.rohan.loggingservice.flink.serialization.OrderEventDeserializer;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
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

import java.math.BigDecimal;

public class FlinkApplication {

    final static String inputTopic = "flinkTopic";
    final static String outputTopic = "flinkOutput";
    final static String jobTitle = "id-processing";

    public static void main(String[] args) throws Exception{

        final String bootstrapServer = "localhost:9092";

        Configuration configuration = new Configuration();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(configuration);

//        env.enableCheckpointing(5000);
//        env.setStateBackend(new EmbeddedRocksDBStateBackend());

        KafkaSource<OrderEvent> kafkaSource = KafkaSource.<OrderEvent>builder()
                .setBootstrapServers(bootstrapServer)
                .setTopics(inputTopic)
                .setGroupId("flinkService")
                .setStartingOffsets(OffsetsInitializer.latest())
                .setValueOnlyDeserializer(new OrderEventDeserializer())
//                .setProperty("isolation.level", "read_committed")
                .build();

        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.builder()
                .setValueSerializationSchema(new SimpleStringSchema())
                .setTopic(outputTopic)
                .build();

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers(bootstrapServer)
//                .setRecordSerializer(new OrderEventSerializer("flinkOutput"))
                .setRecordSerializer(serializer)
//                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .build();


        DataStream<OrderEvent> stream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source");

        // Showing orderEvents that are processing
        DataStream<String> streamProcessingFunction = stream
                .map(orderEvent -> orderEvent + " PROCESSING")
                .setParallelism(1);

        streamProcessingFunction.print("\n").setParallelism(1);

        // KeySelector for keying according to orderStatus
        KeySelector<OrderEvent, String> keySelector = new KeySelector<OrderEvent, String>() {
            @Override
            public String getKey(OrderEvent orderEvent) throws Exception {
                return orderEvent.getOrderStatus();
            }
        };

        KeyedStream<OrderEvent, String> keyedStream = stream.keyBy(keySelector);

        DataStream<Tuple2<String, Long>> resultConfirmed = keyedStream
                .filter(new FilterFunction<OrderEvent>() {
                    @Override
                    public boolean filter(OrderEvent orderEvent) throws Exception {
                        return "CONFIRMED".equals(orderEvent.getOrderStatus());
                    }
                })
                .map(new MapFunction<OrderEvent, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(OrderEvent orderEvent) throws Exception {
                        return new Tuple2<>("CONFIRMED", 1L);
                    }
                })
                .keyBy(tuple -> tuple.f0)
                .sum(1);

        DataStream<Tuple2<String, Long>> resultFailed = keyedStream
                .filter(new FilterFunction<OrderEvent>() {
                    @Override
                    public boolean filter(OrderEvent orderEvent) throws Exception {
                        return "FAILED".equals(orderEvent.getOrderStatus());
                    }
                })
                .map(new MapFunction<OrderEvent, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> map(OrderEvent orderEvent) throws Exception {
                        return new Tuple2<>("FAILED", 1L);
                    }
                })
                .keyBy(tuple -> tuple.f0)
                .sum(1);

        DataStream<BigDecimal> resultTotalRevenue = keyedStream
//                .filter(new FilterFunction<OrderEvent>() {
//                    @Override
//                    public boolean filter(OrderEvent orderEvent) throws Exception {
//                        return "CONFIRMED".equals(orderEvent.getOrderStatus());
//                    }
//                })
                .map(new MapFunction<OrderEvent, BigDecimal>() {
                    @Override
                    public BigDecimal map(OrderEvent orderEvent) throws Exception {
                        return orderEvent.getTotalOrderValue(); // No need to convert to BigDecimal
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
                });

//      Print the aggregated results
        resultConfirmed.print().setParallelism(1);
        resultFailed.print().setParallelism(1);
        resultTotalRevenue.print("Total Revenue =").setParallelism(1);

//        DataStream<Tuple2<String, Long>> combinedResult = resultConfirmed.union(resultFailed);
//        combinedResult.print();

        // orderEvents that have been processed
        DataStream<String> streamProcessedFunction = stream
                .map(orderEvent -> "OrderEvent(" + orderEvent.getOrderNumber() + ") PROCESSED")
                .setParallelism(1);

        // publish processed events to sink flinkOutput
        streamProcessedFunction.sinkTo(kafkaSink);

        env.execute(jobTitle);
    }

}
