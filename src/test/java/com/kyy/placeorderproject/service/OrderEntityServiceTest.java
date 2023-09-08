package com.kyy.placeorderproject.service;

import com.kyy.placeorderproject.domain.OrderEntity;
import com.kyy.placeorderproject.domain.ProductEntity;
import com.kyy.placeorderproject.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderEntityServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("멀티스레드_동시접근_경쟁테스트")
    @Order(1)
    public void 멀티스레드_동시접근_경쟁테스트() throws InterruptedException {
        // 초기 제품 생성
        ProductEntity product = new ProductEntity();
        product.setName("Product1");
        product.setStock(1000);
        productRepository.save(product);

        int numberOfThreads = 100;
        int ordersPerThread = 10;

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < ordersPerThread; j++) {
                        OrderEntity order = new OrderEntity(null, product.getId(), 1);
                        orderService.placeOrder(order);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // 모든 쓰레드 시작
        endLatch.await(); // 모든 쓰레드가 끝날 때까지 대기

        ProductEntity finalProduct = productRepository.findById(product.getId()).orElse(null);
        assertEquals(0, finalProduct.getStock());
    }

    @Test
    @DisplayName("soldout_재고_테스트")
    @Order(2)
    public void soldout_재고_테스트() {
        // 상품 재고를 10개로 설정
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName("Product1");
        productEntity.setStock(10); // 10일때 실패, 20일때 성공
        productRepository.save(productEntity);

        // 첫 번째 주문 (10개)
        OrderEntity order = new OrderEntity();
        order.setProductId(productEntity.getId());
        order.setQuantity(10);
        orderService.placeOrder(order);

        // 두 번째 주문 (5개) - Sold Out 상황
        OrderEntity order1 = new OrderEntity();
        order1.setProductId(productEntity.getId());
        order1.setQuantity(5);

        // Sold Out 예외가 발생해야 함
        Exception exception = assertThrows(RuntimeException.class, () -> orderService.placeOrder(order1));


        // 예외 메시지가 "Sold Out"인지 확인
        assertEquals("Sold Out", exception.getMessage());
    }

    @Test
    @DisplayName("데드락_테스트")
    @Order(3)
    public void 데드락_테스트() throws InterruptedException {
// 초기 제품 생성
        ProductEntity productA = new ProductEntity();
        productA.setName("Product1");
        productA.setStock(1000); // 10일때 실패, 20일때 성공
        ProductEntity productB = new ProductEntity();
        productA.setName("Product2");
        productA.setStock(1000); // 10일때 실패, 20일때 성공
        productRepository.save(productA);
        productRepository.save(productB);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        // 정방향
        Thread thread1 = new Thread(() -> {
            try {
                startLatch.await();
                orderService.placeOrder(new OrderEntity(null, productA.getId(), 1));
                Thread.sleep(1000); // 잠시 대기하여 데드락 발생 확률을 높임
                orderService.placeOrder(new OrderEntity(null, productB.getId(), 1));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                endLatch.countDown();
            }
        });

        // 역방향
        Thread thread2 = new Thread(() -> {
            try {
                startLatch.await();
                orderService.placeOrder(new OrderEntity(null, productB.getId(), 1));
                Thread.sleep(1000); // 잠시 대기하여 데드락 발생 확률을 높임
                orderService.placeOrder(new OrderEntity(null, productA.getId(), 1));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                endLatch.countDown();
            }
        });

        thread1.start();
        thread2.start();

        startLatch.countDown(); // 모든 쓰레드 시작
        endLatch.await(); // 모든 쓰레드가 끝날 때까지 대기

    }
}