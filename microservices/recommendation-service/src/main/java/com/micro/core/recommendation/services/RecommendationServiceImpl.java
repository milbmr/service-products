package com.micro.core.recommendation.services;

import java.util.List;

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
  public Recommendation createRecommendation(Recommendation recommendation) {
    try {
      RecommendationEntity entity = mapper.apiToEntity(recommendation);
      RecommendationEntity savedEntity = repository.save(entity);

      LOG.debug("Creating a recommendation for the prodcut id: " + recommendation.getProductId());
      return mapper.entityToApi(savedEntity);
    } catch (DuplicateKeyException dke) {
      throw new InvalidInputException("Duplicate key, prodcut id: " + recommendation.getProductId());
    }
  }

  @Override
  public List<Recommendation> getRecommendations(int productId) {

    if (productId < 1) {
      throw new InvalidInputException("Invalid product id" + productId);
    }

    List<RecommendationEntity> entityList = repository.findByProductId(productId);
    List<Recommendation> apiList = mapper.entityListToApiList(entityList);
    apiList.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    LOG.debug("Creating recommendation list from entity for product id: ", productId);

    return apiList;
  }

  @Override
  public void deleteRecommendations(int productId) {
    LOG.debug("Deleting recommendation list of product id: ", productId);
    repository.deleteAll(repository.findByProductId(productId));
  }

}
