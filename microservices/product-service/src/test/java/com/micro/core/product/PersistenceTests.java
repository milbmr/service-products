package com.micro.core.product;

import static java.util.stream.IntStream.rangeClosed;
import static org.springframework.data.domain.Sort.Direction.ASC;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

  @Test
  void page() {

    repository.deleteAll();

    List<ProductEntity> products = rangeClosed(1001, 1010)
        .mapToObj(i -> new ProductEntity(i, "name" + i, i)).collect(Collectors.toList());
    repository.saveAll(products);

    Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
    nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
    nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
    nextPage = testNextPage(nextPage, "[1009, 1010]", false);
  }

  private Pageable testNextPage(Pageable nextPage, String expectedPage, boolean nextPageExists) {
    Page<ProductEntity> products = repository.findAll(nextPage);
    assertEquals(expectedPage, products.getContent().stream()
        .map(p -> p.getProductId()).collect(Collectors.toList()).toString());
    assertEquals(nextPageExists, products.hasNext());
    return products.nextPageable();
  }

  private void assertEqualsProduct(ProductEntity assertedEntity, ProductEntity actualEntity) {
    assertEquals(assertedEntity.getId(), actualEntity.getId());
    assertEquals(assertedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(assertedEntity.getName(), actualEntity.getName());
    assertEquals(assertedEntity.getProductId(), actualEntity.getProductId());
    assertEquals(assertedEntity.getWeight(), actualEntity.getWeight());
  }
}
