package com.micro.api.core.product;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

public interface ProductService {

  @PostMapping(value = "/product", produces = "application/json", consumes = "application/json")
  Mono<Product> createProduct(@RequestBody Product body);

  @GetMapping(value = "/product/{productId}", produces = "application/json")
  Mono<Product> getProduct(@PathVariable("productId") int productId);

  @DeleteMapping(value = "/product/{productId}")
  Mono<Void> deleteProduct(@PathVariable("productId") int productId);
}
