package com.micro.core.review.services;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.micro.api.core.review.Review;
import com.micro.api.core.review.ReviewService;
import com.micro.api.event.Event;
import com.micro.api.exceptions.EventProcessingException;

@Configuration
public class MessageProcessorConfig {
  private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  ReviewService reviewService;

  public MessageProcessorConfig(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  @Bean
  Consumer<Event<Integer, Review>> messageProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE:
          reviewService.createReview(event.getData()).block();
          LOG.info("Creation of review message done");
          break;
        case DELETE:
          reviewService.deleteReviews(event.getKey()).block();
          LOG.info("Deleting of review done");
          break;

        default:
          String errorMessage = "Invalid event type " + event.getEventType() + "expected CREATE or DELETE";
          LOG.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("Review Event processing done");
    };
  }
}
