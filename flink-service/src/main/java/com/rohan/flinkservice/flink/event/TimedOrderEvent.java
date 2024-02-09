package com.rohan.flinkservice.flink.event;

import lombok.*;

import java.math.BigDecimal;

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

    public TimedOrderEvent(OrderEvent orderEvent, Long timestamp){
        this.orderNumber = orderEvent.getOrderNumber();
        this.orderEmail = orderEvent.getOrderEmail();
        this.orderStatus = orderEvent.getOrderStatus();
        this.totalOrderValue = orderEvent.getTotalOrderValue();
        this.timestamp = timestamp;
    }
}
