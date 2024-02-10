//package com.rohan.flinkservice.flink.event;
//
//import lombok.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class TimedOrderEvent{
//
//    private String orderNumber;
//    private String orderEmail;
//    private String orderStatus;
//    private BigDecimal totalOrderValue;
//    private Long timestamp;
//    private LocalDateTime localDateTime;
//
//
//    public TimedOrderEvent(OrderEvent orderEvent, Long timestamp, LocalDateTime localDateTime){
//        this.orderNumber = orderEvent.getOrderNumber();
//        this.orderEmail = orderEvent.getOrderEmail();
//        this.orderStatus = orderEvent.getOrderStatus();
//        this.totalOrderValue = orderEvent.getTotalOrderValue();
//        this.timestamp = timestamp;
//        this.localDateTime = localDateTime;
//    }
//
//    public TimedOrderEvent(OrderEvent orderEvent, Long timestamp) {
//        this.orderNumber = orderEvent.getOrderNumber();
//        this.orderEmail = orderEvent.getOrderEmail();
//        this.orderStatus = orderEvent.getOrderStatus();
//        this.totalOrderValue = orderEvent.getTotalOrderValue();
//        this.timestamp = timestamp;
//    }
//}


package com.rohan.flinkservice.flink.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimedOrderEvent{

    private String orderNumber;
    private String orderEmail;
    private String orderStatus;
    private BigDecimal totalOrderValue;
    private Long timestamp;
    private Instant instant;

    public TimedOrderEvent(OrderEvent orderEvent, Long timestamp, Instant instant){
        this.orderNumber = orderEvent.getOrderNumber();
        this.orderEmail = orderEvent.getOrderEmail();
        this.orderStatus = orderEvent.getOrderStatus();
        this.totalOrderValue = orderEvent.getTotalOrderValue();
        this.timestamp = timestamp;
        this.instant = instant;
    }
}