package com.rohan.orderservice.service;


import com.rohan.orderservice.dto.InventoryResponse;
import com.rohan.orderservice.dto.OrderLineItemsDto;
import com.rohan.orderservice.dto.OrderRequest;
import com.rohan.orderservice.event.OrderEvent;
import com.rohan.orderservice.kafka.FlinkProducer;
import com.rohan.orderservice.kafka.OrderProducer;
import com.rohan.orderservice.model.Order;
import com.rohan.orderservice.model.OrderLineItems;
import com.rohan.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final OrderProducer orderProducer;
    private final FlinkProducer flinkProducer;

    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderEmail(orderRequest.getOrderEmail());


        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .filter(orderLineItemsDto -> orderLineItemsDto.getQuantity() > 0)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

        InventoryResponse[] result = webClient.get()
                .uri("http://localhost:8082/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();


        Boolean allProductsInStock = Arrays.stream(result).allMatch(InventoryResponse::isInStock);

        if(allProductsInStock){
            orderRepository.save(order);
            OrderEvent orderEvent = new OrderEvent(order.getOrderNumber(),order.getOrderEmail(),"CONFIRMED");
            orderProducer.sendMessage(orderEvent);
            flinkProducer.sendMessage(order.getOrderNumber());

            return "Order placed successfully";
        }
        else{
            OrderEvent orderEvent = new OrderEvent(order.getOrderNumber(),order.getOrderEmail(),"FAILED");
            orderProducer.sendMessage(orderEvent);
            System.out.println("Product(s) not in stock");
            throw new IllegalArgumentException("Product is not in stock.");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
