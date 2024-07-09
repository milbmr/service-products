package com.micro.core.review;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
// import org.testcontainers.containers.MySQLContainer;

public abstract class PostgresTestBase {
  private static JdbcDatabaseContainer<?> database = new PostgreSQLContainer<>("postgres");

  static {
    database.start();
  }

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry register) {
    register.add("spring.datasource.url", database::getJdbcUrl);
    register.add("spring.datasource.username", database::getUsername);
    register.add("spring.datasource.password", database::getPassword);
  }
}
