package com.micro.api.core.review;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReviewService {

  Mono<Review> createReview(@RequestBody Review review);

  @GetMapping(value = "/review", produces = "application/json")
  Flux<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);

  Mono<Void> deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}
