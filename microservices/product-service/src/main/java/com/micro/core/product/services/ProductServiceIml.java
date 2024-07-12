package com.micro.core.product.services;

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
  public Product createProduct(Product product) {
    try {
      ProductEntity mappedEntity = mapper.apiToEntity(product);
      ProductEntity entity = repository.save(mappedEntity);

      LOG.debug("Creating a new prodcut of id: " + product.getProductId());
      return mapper.entityToApi(entity);
    } catch (DuplicateKeyException dke) {
      throw new InvalidInputException("Duplicate key, product id: " + product.getProductId());
    }

  }

  @Override
  public Product getProduct(int productId) {
    if (productId < 1) {
      throw new InvalidInputException("Invalid product Id: " + productId);
    }

    ProductEntity entity = repository.findByProductId(productId)
        .orElseThrow(() -> new NotFoundException("No product found " + productId));

    Product respose = mapper.entityToApi(entity);
    respose.setServiceAddress(serviceUtil.getServiceAddress());
    LOG.debug("Getting product of id: " + productId);
    return respose;
  }

  @Override
  public void deleteProduct(int productId) {
    LOG.debug("Trying to delete a product of id: " + productId);
    repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
  }

}
