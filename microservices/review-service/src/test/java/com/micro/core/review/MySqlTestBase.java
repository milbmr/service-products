package com.micro.core.review;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

public abstract class MySqlTestBase {
  @SuppressWarnings({ "rawtypes", "resource" })
  private static JdbcDatabaseContainer database = new MySQLContainer("mysql:x.x.x").withConnectTimeoutSeconds(300);

  static {
    database.start();
  }

  static void databaseProperties(DynamicPropertyRegistry register) {
    register.add("spring.datasource.url", database::getJdbcUrl);
    register.add("spring.datasource.username", database::getUsername);
    register.add("spring.datasource.password", database::getPassword);
  }
}
