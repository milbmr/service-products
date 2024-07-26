package com.micro.core.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("com.micro")
public class ReviewServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);

  private final Integer threadPool;
  private final Integer threadQueue;

  public ReviewServiceApplication(
      @Value("${app.threadPool:10}") Integer threadPool,
      @Value("${app.threadQueue:100}") Integer threadQueue) {
    this.threadPool = threadPool;
    this.threadQueue = threadQueue;
  }

  @Bean
  Scheduler jdbcScheduler() {
    LOG.info("Creates a jdbcSchduler with thread pool size {} " + threadPool);
    return Schedulers.newBoundedElastic(threadPool, threadQueue, "jdbc-pool");
  }

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(ReviewServiceApplication.class, args);

    String postgresUrl = ctx.getEnvironment().getProperty("spring.datasource.url");

    LOG.info("Connected to postgres: " + postgresUrl);
  }

}
