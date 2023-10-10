package com.yash357.orderservice.controller;

import com.yash357.orderservice.dto.OrderRequest;
import com.yash357.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name= "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name="inventory")
    @Retry(name="inventory")
    public CompletableFuture<String> placeOrder(@RequestBody OrderRequest orderRequest) {
        assert orderRequest != null;
        log.info("The order request is in place order" + orderRequest.getOrderLineItemsDtoList().get(0).toString() + " here");
        return CompletableFuture.supplyAsync(()->orderService.placeOrder(orderRequest));
//        return CompletableFuture.supplyAsync(() ->"Order Placed Successfully");
    }

    public CompletableFuture<String> fallbackMethod(OrderRequest orderRequest, RuntimeException runtimeException) {
        return CompletableFuture.supplyAsync(()->"Oops! Something went wrong. Please order after some time.");
    }
}
