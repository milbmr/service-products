package com.micro.core.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;

import com.micro.core.product.persistence.ProductEntity;
import com.micro.core.product.persistence.ProductRepository;
import com.mongodb.DuplicateKeyException;

//starts a database automaticlly
@DataMongoTest
public class PersistenceTests extends MongoTestBase {
  @Autowired
  private ProductRepository repository;
  private ProductEntity savedEntity;

  @BeforeEach
  void setUp() {
    repository.deleteAll();

    ProductEntity entity = new ProductEntity(1, "n", 1);
    savedEntity = repository.save(entity);
    assertEqualsProduct(entity, savedEntity);
  }

  @Test
  void create() {
    ProductEntity entity = new ProductEntity(2, "n", 2);
    repository.save(entity);

    ProductEntity foundEntity = repository.findById(entity.getId()).get();
    assertEqualsProduct(entity, foundEntity);

    assertEquals(2, repository.count());
  }

  @Test
  void update() {
    savedEntity.setName("n2");
    repository.save(savedEntity);

    ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (long) foundEntity.getVersion());
    assertEquals("n2", foundEntity.getName());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);
    assertFalse(repository.existsById(savedEntity.getId()));
  }

  @Test
  void getByProductId() {
    Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());

    assertTrue(entity.isPresent());
    assertEqualsProduct(savedEntity, entity.get());
  }

  @Test
  void dublicateKeyError() {
    assertThrows(DuplicateKeyException.class, () -> {
      ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
      repository.save(entity);
    });
  }

  @Test
  void OptimisticLockError() {
    ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
    ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

    entity1.setName("n1");
    repository.save(entity1);

    assertThrows(OptimisticLockingFailureException.class, () -> {
      entity2.setName("n2");
      repository.save(entity2);
    });

    ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (long) updatedEntity.getVersion());
    assertEquals("n1", updatedEntity.getName());
  }

  private void assertEqualsProduct(ProductEntity assertedEntity, ProductEntity actualEntity) {
    assertEquals(assertedEntity.getId(), actualEntity.getId());
    assertEquals(assertedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(assertedEntity.getName(), actualEntity.getName());
    assertEquals(assertedEntity.getProductId(), actualEntity.getProductId());
    assertEquals(assertedEntity.getWeight(), actualEntity.getWeight());
  }
}
