package com.micro.api.core.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface ProductService {
    @GetMapping(value = "product/{procductId}", produces = "applocation/json")
    Product getProduct(@PathVariable("productId") int productId);
}
