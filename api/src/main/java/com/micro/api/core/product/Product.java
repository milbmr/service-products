package com.micro.api.core.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Product {
    @Getter
    private final int productId;
    @Getter
    private final String name;
    @Getter
    private final int weight;
    @Getter
    private final String serviceAddress;

    public Product() {
        productId = 0;
        name = null;
        weight = 0;
        serviceAddress = null;
    }
}
