package com.rohan.flinkservice.flink;

import java.time.*;

public class FlinkTest {
    public static void main(String[] args) {

        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        System.out.println(zonedDateTime);
        System.out.println(zonedDateTime.toInstant());

    }
}