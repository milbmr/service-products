package com.micro.core.product;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.micro.core.product.persistence.ProductEntity;
import com.micro.core.product.persistence.ProductRepository;

//starts a database automaticlly
@DataMongoTest
public class PersistenceTests extends MongoTestBase{
  @Autowired
  private ProductRepository repository;
  private ProductEntity savedEntity;

  @BeforeEach
  void setUp() {

  }
}
