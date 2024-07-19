package com.micro.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecommendationService {

  @PostMapping(value = "/recommendation", produces = "application/json", consumes = "application/json")
  Mono<Recommendation> createRecommendation(@RequestBody Recommendation recommendation);

  @GetMapping(value = "/recommendation", produces = "application/json")
  Flux<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

  @DeleteMapping(value = "/recommendation")
  Mono<Void> deleteRecommendations(@RequestParam(value = "productId", required = true) int prodcutId);
}