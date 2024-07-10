package com.micro.core.review.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import com.micro.api.core.review.Review;
import com.micro.api.core.review.ReviewService;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.core.review.persistence.ReviewEntity;
import com.micro.core.review.persistence.ReviewRepository;
import com.micro.util.http.ServiceUtil;

@RestController
public class ReviewServiceImpl implements ReviewService {
  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final ReviewRepository repository;
  private final ReviewMapper mapper;

  public ReviewServiceImpl(ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Review createReview(Review review) {
    try {
      ReviewEntity entity = mapper.apiToEntity(review);
      ReviewEntity newEntity = repository.save(entity);

      LOG.debug("Creating review of product id: " + review.getProductId());
      return mapper.entityToApi(newEntity);
    } catch (DuplicateKeyException dke) {
      throw new InvalidInputException("Duplicate key, for prodcut id: " + review.getProductId());
    }
  }

  @Override
  public List<Review> getReviews(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid product Id: " + productId);
    }

    List<ReviewEntity> entityList = repository.findByProductId(productId);
    List<Review> apiList = mapper.entityListToApiList(entityList);
    apiList.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    LOG.debug("getReviews reviews size: " + apiList.size());

    return apiList;
  }

  @Override
  public void deleteReviews(int productId) {
    LOG.debug("Deleting reviews");
    repository.deleteAll(repository.findByProductId(productId));
  }
}
