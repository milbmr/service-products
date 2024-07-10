package com.micro.api.core.product;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface ProductService {

  @PostMapping(value = "/product", produces = "application/json", consumes = "application/json")
  Product createProduct(@RequestBody Product body);

  @GetMapping(value = "/product/{productId}", produces = "application/json")
  Product getProduct(@PathVariable("productId") int productId);

  @DeleteMapping(value = "/product/{productId}")
  void deleteProduct(@PathVariable("productId") int productId);
}
