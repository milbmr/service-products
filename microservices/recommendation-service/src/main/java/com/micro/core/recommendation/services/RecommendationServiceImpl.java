package com.micro.core.recommendation.services;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.recommendation.RecommendationService;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.core.recommendation.persistence.RecommendationEntity;
import com.micro.core.recommendation.persistence.RecommendationRepository;
import com.micro.util.http.ServiceUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final RecommendationRepository repository;
  private final RecommendationMapper mapper;

  public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper,
      ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Mono<Recommendation> createRecommendation(Recommendation recommendation) {
    if (recommendation.getProductId() < 1) {
      throw new InvalidInputException("Invalid product id" + recommendation.getProductId());
    }

    RecommendationEntity entity = mapper.apiToEntity(recommendation);
    Mono<Recommendation> savedEntity = repository.save(entity).log(LOG.getName(), FINE)
        .onErrorMap(DuplicateKeyException.class,
            ex -> new InvalidInputException("Duplicate key, prodcut id: " + recommendation.getProductId()))
        .map(e -> mapper.entityToApi(e));

    LOG.debug("Creating a recommendation for the prodcut id: " + recommendation.getProductId());
    return savedEntity;
  }

  @Override
  public Flux<Recommendation> getRecommendations(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid product id" + productId);
    }

    Flux<Recommendation> entityList = repository.findByProductId(productId)
        .log(LOG.getName(), FINE).map(e -> mapper.entityToApi(e)).map(e -> {
          e.setServiceAddress(serviceUtil.getServiceAddress());
          return e;
        });

    LOG.debug("Creating recommendation list from entity for product id: ", productId);

    return entityList;
  }

  @Override
  public Mono<Void> deleteRecommendations(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid product id" + productId);
    }

    LOG.debug("Deleting recommendation list of product id: ", productId);
    return repository.deleteAll(repository.findByProductId(productId));
  }
}
