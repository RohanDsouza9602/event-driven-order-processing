package com.rohan.flinkservice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderEvent {
    private String orderNumber;
    private String orderEmail;
    private String orderStatus;
    private BigDecimal totalOrderValue;
}
