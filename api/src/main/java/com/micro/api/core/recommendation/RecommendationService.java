package com.micro.api.core.recommendation;

import java.util.List;

import org.springframework.web.bind.annotation.*;

public interface RecommendationService {

  @PostMapping(value = "/recommendation", produces = "application/json", consumes = "application/json")
  Recommendation createRecommendation(@RequestBody Recommendation recommendation);

  @GetMapping(value = "/recommendation", produces = "application/json")
  List<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

  @DeleteMapping(value = "/recommendation")
  void deleteRecommendations(@RequestParam(value = "prodcutId", required = true) int prodcutId);
}