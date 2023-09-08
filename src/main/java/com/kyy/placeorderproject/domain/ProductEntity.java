package com.kyy.placeorderproject.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int stock;

    public void decreaseStock(int quantity) {

        if (this.stock <= 0) {
            throw new RuntimeException("Sold Out");
        }

        this.stock -= quantity;
    }

    // 동시성 처리
    @Version
    private int version;
}
