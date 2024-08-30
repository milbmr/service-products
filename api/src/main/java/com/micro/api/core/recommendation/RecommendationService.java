package com.micro.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {

  Mono<Recommendation> createRecommendation(@RequestBody Recommendation recommendation);

  @GetMapping(value = "/recommendation", produces = "application/json")
  Flux<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

  Mono<Void> deleteRecommendations(@RequestParam(value = "productId", required = true) int prodcutId);
}