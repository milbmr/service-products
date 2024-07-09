package com.micro.core.review;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import com.micro.core.review.persistence.ReviewEntity;
import com.micro.core.review.persistence.ReviewRepository;

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersistenceTests extends PostgresTestBase {
  @Autowired
  private ReviewRepository repository;
  private ReviewEntity savedEntity;

  @BeforeEach
  void setUp() {
    repository.deleteAll();

    ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
    savedEntity = repository.save(entity);

    assertEqualReview(entity, savedEntity);
  }

  @Test
  void create() {
    ReviewEntity entity = new ReviewEntity(1, 3, "a", "s", "c");
    repository.save(entity);

    ReviewEntity foundEntity = repository.findById(entity.getId()).get();
    assertEqualReview(entity, foundEntity);
    assertEquals(2, repository.count());
  }

  @Test
  void update() {
    savedEntity.setAuthor("a2");
    repository.save(savedEntity);

    ReviewEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, foundEntity.getVersion());
    assertEquals("a2", foundEntity.getAuthor());
  }

  @Test
  void delete() {
    repository.delete(savedEntity);
    assertFalse(repository.existsById(savedEntity.getId()));
  }

  @Test
  void findByProductId() {
    List<ReviewEntity> entityList = repository.findByProductId(savedEntity.getProductId());

    assertThat(entityList, hasSize(1));
    assertEqualReview(savedEntity, entityList.get(0));
  }

  @Test
  void duplicateError() {
    assertThrows(DataIntegrityViolationException.class, () -> {
      ReviewEntity entity = new ReviewEntity(1, 2, "a", "s", "c");
      repository.save(entity);
    });
  }

  @Test
  void optimisticLockError() {
    ReviewEntity entity1 = repository.findById(savedEntity.getId()).get();
    ReviewEntity entity2 = repository.findById(savedEntity.getId()).get();

    entity1.setAuthor("a1");
    repository.save(entity1);

    assertThrows(OptimisticLockingFailureException.class, () -> {
      entity2.setAuthor("a2");
      repository.save(entity2);
    });

    ReviewEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, updatedEntity.getVersion());
    assertEquals("a1", updatedEntity.getAuthor());
  }

  private void assertEqualReview(ReviewEntity expectedEntity, ReviewEntity actualEntity) {
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
    assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
    assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
    assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
  }
}
