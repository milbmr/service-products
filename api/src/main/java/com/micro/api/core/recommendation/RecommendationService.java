package com.micro.api.core.recommendation;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface RecommendationService {

  @PostMapping(value = "/recommendation", produces = "application/json", consumes = "application/json")
  Recommendation createRecommendation(@RequestBody Recommendation recommendation);

  @GetMapping(value = "/recommendation", produces = "application/json")
  List<Recommendation> getRecommendations(@RequestParam(value = "productId", required = true) int productId);

  @DeleteMapping(value = "/recommendation")
  void deleteRecommendations(@RequestParam(value = "prodcutId", required = true) int prodcutId);
}