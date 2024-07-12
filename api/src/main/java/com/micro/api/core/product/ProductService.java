package com.micro.api.core.product;

import org.springframework.web.bind.annotation.*;

public interface ProductService {

  @PostMapping(value = "/product", produces = "application/json", consumes = "application/json")
  Product createProduct(@RequestBody Product body);

  @GetMapping(value = "/product/{productId}", produces = "application/json")
  Product getProduct(@PathVariable("productId") int productId);

  @DeleteMapping(value = "/product/{productId}")
  void deleteProduct(@PathVariable("productId") int productId);
}
