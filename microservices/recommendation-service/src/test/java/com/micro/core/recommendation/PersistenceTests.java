package com.micro.core.recommendation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.micro.core.recommendation.persistence.RecommendationEntity;
import com.micro.core.recommendation.persistence.RecommendationRepository;

@DataMongoTest
public class PersistenceTests extends MongoTestBase {
  @Autowired
  private RecommendationRepository repository;
  private RecommendationEntity savedEntity;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
    RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
    savedEntity = repository.save(entity);

    assertEquals(entity, savedEntity);
  }

  @Test
  void create() {
    RecommendationEntity entity = new RecommendationEntity(1, 3, "a", 3, "c");
    repository.save(entity);

    RecommendationEntity foundEntity = repository.findById(entity.getId()).get();
    assertEqualRecommendation(entity, foundEntity);
    assertEquals(2, repository.count());
  }

  @Test
  void update() {
    savedEntity.setAuthor("a2");
    repository.save(savedEntity);

    RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, foundEntity.getVersion());
    assertEquals(savedEntity.getAuthor(), foundEntity.getAuthor());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);
    assertFalse(repository.existsById(savedEntity.getId()));
  }

  @Test
  void findByProductId() {
    List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId());

    assertThat(entityList, hasSize(1));
    assertEqualRecommendation(savedEntity, entityList.get(0));
  }

  @Test
  void duplicateError() {
    assertThrows(DuplicateKeyException.class, () -> {
      RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
      repository.save(entity);
    });
  }

  @Test
  void OptimisticLockError() {
    RecommendationEntity entity1 = repository.findById(savedEntity.getId()).get();
    RecommendationEntity entity2 = repository.findById(savedEntity.getId()).get();

    entity1.setAuthor("a1");
    repository.save(entity1);

    assertThrows(OptimisticLockingFailureException.class, () -> {
      entity2.setAuthor("a2");
      repository.save(entity2);
    });

    RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, updatedEntity.getVersion());
    assertEquals("a1", updatedEntity.getAuthor());
  }

  private void assertEqualRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
    assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
    assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
    assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    assertEquals(expectedEntity.getRating(), actualEntity.getRating());
  }

}
