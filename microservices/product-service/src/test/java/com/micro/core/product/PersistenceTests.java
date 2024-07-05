package com.micro.core.product;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.micro.core.product.persistence.ProductEntity;
import com.micro.core.product.persistence.ProductRepository;

//starts a database automaticlly
@DataMongoTest
public class PersistenceTests extends MongoTestBase {
  @Autowired
  private ProductRepository repository;
  private ProductEntity savedEntity;

  @BeforeEach
  void setUp() {
    repository.deleteAll();

    ProductEntity entity = new ProductEntity(1, "product 1", 45);
    savedEntity = repository.save(entity);
    assertEqualsProduct(entity, savedEntity);
  }

  private void assertEqualsProduct(ProductEntity assertedEntity, ProductEntity actualEntity) {
    assertEquals(assertedEntity.getId(), actualEntity.getId());
    assertEquals(assertedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(assertedEntity.getName(), actualEntity.getName());
    assertEquals(assertedEntity.getProductId(), actualEntity.getProductId());
    assertEquals(assertedEntity.getWeight(), actualEntity.getWeight());
  }
}
