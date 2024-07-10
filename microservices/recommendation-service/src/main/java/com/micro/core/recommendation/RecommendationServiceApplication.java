package com.micro.core.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.micro")
public class RecommendationServiceApplication {

  private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceApplication.class);

  public static void main(String[] args) {
    ConfigurableApplicationContext ctx = SpringApplication.run(RecommendationServiceApplication.class, args);

    String mongodbHost = ctx.getEnvironment().getProperty("spring.data.mongodb.host");
    String mongodbPort = ctx.getEnvironment().getProperty("spring.data.mongodb.port");

    LOG.info("Connected to mongodb: " + mongodbHost + ":" + mongodbPort);
  }

}
