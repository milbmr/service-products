package com.micro.api.core.review;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface ReviewService {

  @PostMapping(value = "/review", produces = "application/json", consumes = "application/json")
  Review createReview(@RequestBody Review review);

  @GetMapping(value = "/review", produces = "application/json")
  List<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);

  @DeleteMapping(value = "/review")
  void deleteReviews(@RequestParam(value = "productId", required = true) int productId);
}
