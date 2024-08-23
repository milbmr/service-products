package com.micro.composite.product;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan("com.micro")
public class ProductCompositeServiceApplication {
  private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeServiceApplication.class);

  @Value("${api.common.version}")
  String apiVersion;
  @Value("${api.common.title}")
  String apiTitle;
  @Value("${api.common.description}")
  String apiDescription;
  @Value("${api.common.termsOfService}")
  String apiTermsOfService;
  @Value("${api.common.license}")
  String apiLicense;
  @Value("${api.common.licenseUrl}")
  String apiLicenseUrl;
  @Value("${api.common.externalDocDesc}")
  String apiExternalDocDesc;
  @Value("${api.common.externalDocUrl}")
  String apiExternalDocUrl;
  @Value("${api.common.contact.name}")
  String apiContactName;
  @Value("${api.common.contact.url}")
  String apiContactUrl;
  @Value("${api.common.contact.email}")
  String apiContactEmail;

  @Bean
  OpenAPI getOpenAPIDocumentation() {
    return new OpenAPI()
        .info(new Info().title(apiTitle)
            .description(apiDescription)
            .version(apiVersion)
            .contact(new Contact().name(apiContactName).url(apiContactUrl).email(apiContactEmail))
            .termsOfService(apiTermsOfService)
            .license(new License().name(apiLicense).url(apiLicenseUrl)))
        .externalDocs(new ExternalDocumentation().description(apiExternalDocDesc).url(apiExternalDocUrl));
  }

  @Bean
  RestTemplate restTemplate() {
    return new RestTemplate();
  }

  private final Integer threadPoolSize;
  private final Integer taskQueSize;

  public ProductCompositeServiceApplication(@Value("app.threadPoolSize:10") Integer threadPoolSize,
      @Value("app.taskQueSize:100") Integer taskQueSize) {
    this.threadPoolSize = threadPoolSize;
    this.taskQueSize = taskQueSize;
  }

  @Bean
  Scheduler publisherEventScheduler() {
    LOG.info("Creating messagingScheduler with threadPoolSize {}", threadPoolSize);
    return Schedulers.newBoundedElastic(threadPoolSize, taskQueSize, "publish-pool");
  }

  public static void main(String[] args) {
    SpringApplication.run(ProductCompositeServiceApplication.class, args);
  }

}
