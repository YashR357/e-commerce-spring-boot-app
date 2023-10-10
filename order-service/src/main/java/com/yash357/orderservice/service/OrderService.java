package com.yash357.orderservice.service;

import com.yash357.orderservice.dto.InventoryResponse;
import com.yash357.orderservice.dto.OrderLineItemsDto;
import com.yash357.orderservice.dto.OrderRequest;
import com.yash357.orderservice.model.Order;
import com.yash357.orderservice.model.OrderLineItems;
import com.yash357.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        log.info("The order request is" + orderRequest.getOrderLineItemsDtoList());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();
        log.info("The line items are" + orderLineItems.get(0).getQuantity());
        order.setOrderLineItemsList(orderLineItems);
        log.info(String.valueOf(order.getId()));
        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode)
                .toList();

        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes ).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsAreInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);
        log.info(String.valueOf(allProductsAreInStock));
        if (allProductsAreInStock) {
            orderRepository.save(order);
            return "Order placed successfully";
        } else {
            throw new IllegalArgumentException("Product is not in stock");
        }
//        orderRepository.save(order);

    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
