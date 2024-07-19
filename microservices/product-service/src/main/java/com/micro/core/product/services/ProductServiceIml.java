package com.micro.core.product.services;

import static java.util.logging.Level.FINE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import com.micro.api.core.product.Product;
import com.micro.api.core.product.ProductService;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.api.exceptions.NotFoundException;
import com.micro.core.product.persistence.ProductEntity;
import com.micro.core.product.persistence.ProductRepository;
import com.micro.util.http.ServiceUtil;

import reactor.core.publisher.Mono;

@RestController
public class ProductServiceIml implements ProductService {
  private static final Logger LOG = LoggerFactory.getLogger(ProductServiceIml.class);

  private final ServiceUtil serviceUtil;
  private final ProductRepository repository;
  private final ProductMapper mapper;

  public ProductServiceIml(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Mono<Product> createProduct(Product product) {
    if (product.getProductId() < 1) {
      throw new InvalidInputException("Invalid product Id: " + product.getProductId());
    }

    LOG.debug("Creating a new prodcut of id: " + product.getProductId());

    ProductEntity mappedEntity = mapper.apiToEntity(product);
    Mono<Product> entity = repository.save(mappedEntity).log(LOG.getName(), FINE)
        .onErrorMap(DuplicateKeyException.class,
            ex -> new InvalidInputException("Duplicate key, product id: " + product.getProductId()))
        .map(e -> mapper.entityToApi(e));

    return entity;
  }

  @Override
  public Mono<Product> getProduct(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid product Id: " + productId);
    }

    Mono<Product> entity = repository.findByProductId(productId)
        .switchIfEmpty(Mono.error(new NotFoundException("No product found " + productId)))
        .log(LOG.getName(), FINE)
        .map(foundEntity -> mapper.entityToApi(foundEntity))
        .map(e -> {
          e.setServiceAddress(serviceUtil.getServiceAddress());
          return e;
        });

    LOG.debug("Getting product of id: " + productId);
    return entity;
  }

  @Override
  public Mono<Void> deleteProduct(int productId) {
    LOG.debug("Trying to delete a product of id: " + productId);
    return repository.findByProductId(productId).log(LOG.getName(), FINE).map(e -> repository.delete(e))
        .flatMap(e -> e);
  }

}
