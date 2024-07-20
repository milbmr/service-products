package com.micro.core.product.services;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.micro.api.core.product.Product;
import com.micro.api.core.product.ProductService;
import com.micro.api.event.Event;
import com.micro.api.exceptions.EventProcessingException;

@Configuration
public class MessageProcessorConfig {
  private final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final ProductService productService;

  public MessageProcessorConfig(ProductService productService) {
    this.productService = productService;
  }

  @Bean
  Consumer<Event<Integer, Product>> messageProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE:
          Product product = event.getDate();
          LOG.info("creating product with id " + event.getKey());
          productService.createProduct(product).block();
          break;
        case DELETE:
          int productId = event.getKey();
          LOG.info("deleting product with product id " + productId);
          productService.deleteProduct(productId).block();
          break;

        default:
          String errorMessage = "Incorrect event type " + event.getEventType() + "expected CREATE or DELETE";
          LOG.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("message processing done");
    };
  }
}
