package com.micro.core.review.services;

import static java.util.logging.Level.FINE;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import com.micro.api.core.review.Review;
import com.micro.api.core.review.ReviewService;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.core.review.persistence.ReviewEntity;
import com.micro.core.review.persistence.ReviewRepository;
import com.micro.util.http.ServiceUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

@RestController
public class ReviewServiceImpl implements ReviewService {
  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final ReviewRepository repository;
  private final ReviewMapper mapper;
  private final Scheduler jdbScheduler;

  public ReviewServiceImpl(ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil,
      @Qualifier("jdbcScheduler") Scheduler jdbScheduler) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
    this.jdbScheduler = jdbScheduler;
  }

  @Override
  public Mono<Review> createReview(Review review) {
    if (review.getProductId() < 1) {
      throw new InvalidInputException("Invalid product Id: " + review.getProductId());
    }

    return Mono.fromCallable(() -> internalCreateReview(review)).subscribeOn(jdbScheduler);
  }

  private Review internalCreateReview(Review review) {
    try {
      ReviewEntity entity = mapper.apiToEntity(review);
      ReviewEntity newEntity = repository.save(entity);

      LOG.debug("Creating review of product id: " + review.getProductId());
      return mapper.entityToApi(newEntity);
    } catch (DataIntegrityViolationException dke) {
      throw new InvalidInputException("Duplicate key, for prodcut id: " + review.getProductId());
    }
  }

  @Override
  public Flux<Review> getReviews(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid product Id: " + productId);
    }

    return Mono.fromCallable(() -> internalGetReviews(productId)).flatMapMany(Flux::fromIterable)
        .log(LOG.getName(), FINE).subscribeOn(jdbScheduler);
  }

  private List<Review> internalGetReviews(int productId) {
    List<ReviewEntity> entityList = repository.findByProductId(productId);
    List<Review> apiList = mapper.entityListToApiList(entityList);
    apiList.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    LOG.debug("getReviews reviews size: " + apiList.size());

    return apiList;
  }

  @Override
  public Mono<Void> deleteReviews(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid product Id: " + productId);
    }

    return Mono.fromRunnable(() -> internalDeleteReviews(productId)).subscribeOn(jdbScheduler).then();
  }

  private void internalDeleteReviews(int productId) {
    LOG.debug("Deleting reviews");
    repository.deleteAll(repository.findByProductId(productId));
  }
}
