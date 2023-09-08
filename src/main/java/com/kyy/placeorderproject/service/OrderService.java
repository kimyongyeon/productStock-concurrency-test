package com.kyy.placeorderproject.service;

import com.kyy.placeorderproject.domain.OrderEntity;
import com.kyy.placeorderproject.domain.ProductEntity;
import com.kyy.placeorderproject.repository.OrderRepository;
import com.kyy.placeorderproject.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    final private EntityManager entityManager; // 트랜잭션 단위로~~~

    @Transactional
    public void placeOrder(OrderEntity order) {
        // race condition - 비관적락은 성공!
        // Pessimistic Locking:
//        ProductEntity productEntity = entityManager.find(ProductEntity.class, order.getProductId(), LockModeType.PESSIMISTIC_WRITE);

        // SELECT FOR UPDATE
        ProductEntity productEntity = productRepository.findProductForUpdate(order.getProductId());

        // race condition - 낙관적락은 실패!
        // Optimistic Locking
//        ProductEntity productEntity = productRepository.findById(order.getProductId()).orElse(null);

        orderRepository.save(order);
        productEntity.decreaseStock(order.getQuantity());

        productRepository.save(productEntity);

        System.out.println("productEntity.getStock() = " + productEntity.getStock());
        System.out.println("order.getQuantity() = " + order.getQuantity());
    }
}
