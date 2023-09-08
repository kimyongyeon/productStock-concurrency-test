package com.kyy.placeorderproject.controller;

import com.kyy.placeorderproject.domain.OrderEntity;
import com.kyy.placeorderproject.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public String placeOrder(@RequestParam Long productId, int quantity) {
        try {
            OrderEntity order = new OrderEntity();
            order.setProductId(productId);
            order.setQuantity(quantity);
            orderService.placeOrder(order);
            return "Order successful";
        } catch (RuntimeException e) {
            return "Order failed: " + e.getMessage();
        }
    }
}
