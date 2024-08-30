package com.micro.composite.product;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.micro.composite.product.services.ProductCompositeIntegration;

@Configuration
public class HealthCheckConfiguration {

  @Autowired
  ProductCompositeIntegration integration;

  @Bean
  ReactiveHealthContributor coreServices() {
    final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

    registry.put("products", () -> integration.getProductHealth());
    registry.put("recommendations", () -> integration.getRecommendationsHealth());
    registry.put("reviews", () -> integration.getReviewsHealth());

    return CompositeReactiveHealthContributor.fromMap(registry);
  }

}
