package com.micro.core.product.services;

import org.springframework.web.bind.annotation.RestController;

import com.micro.api.core.product.Product;
import com.micro.api.core.product.ProductService;
import com.micro.util.http.ServiceUtil;

@RestController
public class ProductServiceIml implements ProductService {
    private final ServiceUtil serviceUtil;

    public ProductServiceIml(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {
        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }

}
