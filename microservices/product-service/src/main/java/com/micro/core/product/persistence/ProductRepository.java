package com.micro.core.product.persistence;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface ProductRepository
    extends PagingAndSortingRepository<ProductEntity, String>, ReactiveCrudRepository<ProductEntity, String> {

  Mono<ProductEntity> findByProductId(int productId);
}
