package com.micro.core.recommendation.services;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.recommendation.RecommendationService;
import com.micro.api.event.Event;
import com.micro.api.exceptions.EventProcessingException;

@Configuration
public class MessageProcessorConfig {
  private final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

  private final RecommendationService recommendationService;

  public MessageProcessorConfig(RecommendationService recommendationService) {
    this.recommendationService = recommendationService;
  }

  @Bean
  Consumer<Event<Integer, Recommendation>> messageProcessor() {
    return event -> {
      switch (event.getEventType()) {
        case CREATE:
          recommendationService.createRecommendation(event.getData()).block();
          LOG.info("Creation message Done");
          break;

        case DELETE:
          recommendationService.deleteRecommendations(event.getKey()).block();
          LOG.info("Deletion message Done");
          break;

        default:
          String errorMessage = "Unkown event type " + event.getEventType() + "expected create or delete";
          LOG.warn(errorMessage);
          throw new EventProcessingException(errorMessage);
      }

      LOG.info("Event processing done");
    };
  }
}
