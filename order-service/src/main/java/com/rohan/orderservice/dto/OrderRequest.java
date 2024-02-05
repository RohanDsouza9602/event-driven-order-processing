package com.rohan.orderservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    private String orderEmail;

    private List<OrderLineItemsDto> orderLineItemsDtoList;

}
