package com.micro.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import com.micro.core.product.persistence.ProductEntity;
import com.micro.core.product.persistence.ProductRepository;
import reactor.test.StepVerifier;

//starts a database automaticlly
@DataMongoTest
public class PersistenceTests extends MongoTestBase {
  @Autowired
  private ProductRepository repository;

  private ProductEntity savedEntity;

  @BeforeEach
  void setUp() {
    StepVerifier.create(repository.deleteAll()).verifyComplete();

    ProductEntity entity = new ProductEntity(1, "n", 1);
    StepVerifier.create(repository.save(entity)).expectNextMatches(createdEntity -> {
      savedEntity = createdEntity;
      return areProductEqual(entity, savedEntity);
    }).verifyComplete();
  }

  @Test
  void create() {
    ProductEntity entity = new ProductEntity(2, "n", 2);
    StepVerifier.create(repository.save(entity))
        .expectNextMatches(savedEntity -> entity.getProductId() == savedEntity.getProductId()).verifyComplete();

    StepVerifier.create(repository.findById(entity.getId()))
        .expectNextMatches(foundEntity -> areProductEqual(entity, foundEntity));

    StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
  }

  @Test
  void update() {
    savedEntity.setName("n2");
    StepVerifier.create(repository.save(savedEntity))
        .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2")).verifyComplete();

    StepVerifier.create(repository.findById(savedEntity.getId()))
        .expectNextMatches(e -> e.getVersion() == 1 && e.getName().equals("n2")).verifyComplete();
  }

  @Test
  void delete() {
    StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
    StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
  }

  @Test
  void getByProductId() {
    StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
        .expectNextMatches(e -> areProductEqual(savedEntity, e)).verifyComplete();
  }

  @Test
  void dublicateKeyError() {
    ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
    StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
  }

  @Test
  void OptimisticLockError() {
    ProductEntity entity1 = repository.findById(savedEntity.getId()).block();
    ProductEntity entity2 = repository.findById(savedEntity.getId()).block();

    entity1.setName("n1");
    repository.save(entity1).block();

    StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

    StepVerifier.create(repository.findById(savedEntity.getId()))
        .expectNextMatches(e -> e.getVersion() == 1 && e.getName() == "n1");
  }

  // @Test
  // void page() {

  // repository.deleteAll();

  // List<ProductEntity> productsPage = rangeClosed(1001, 1010)
  // .mapToObj(i -> new ProductEntity(i, "name" + i,
  // i)).collect(Collectors.toList());
  // repository.saveAll(productsPage);

  // Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
  // nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
  // nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
  // nextPage = testNextPage(nextPage, "[1009, 1010]", false);
  // }

  // private Pageable testNextPage(Pageable nextPage, String expectedPage, boolean
  // nextPageExists) {
  // Page<ProductEntity> productsPage = repository.findAll(nextPage);
  // assertEquals(expectedPage, productsPage.getContent().stream()
  // .map(p -> p.getProductId()).collect(Collectors.toList()).toString());
  // assertEquals(nextPageExists, productsPage.hasNext());
  // return productsPage.nextPageable();
  // }

  // private void assertEqualsProduct(ProductEntity assertedEntity, ProductEntity
  // actualEntity) {
  // assertEquals(assertedEntity.getId(), actualEntity.getId());
  // assertEquals(assertedEntity.getVersion(), actualEntity.getVersion());
  // assertEquals(assertedEntity.getName(), actualEntity.getName());
  // assertEquals(assertedEntity.getProductId(), actualEntity.getProductId());
  // assertEquals(assertedEntity.getWeight(), actualEntity.getWeight());
  // }

  private boolean areProductEqual(ProductEntity expecteProduct, ProductEntity actualProduct) {
    return expecteProduct.getId().equals(actualProduct.getId())
        && (expecteProduct.getVersion().equals(actualProduct.getVersion()))
        && (expecteProduct.getName().equals(actualProduct.getName()))
        && (expecteProduct.getProductId() == actualProduct.getProductId())
        && (expecteProduct.getWeight() == actualProduct.getWeight());
  }
}
