package com.micro.api.composite.product;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "ProductComposite", description = "REST API FOR COMPOSITE PRODUCT SERVICE")
public interface ProductCompositeService {

  @Operation(summary = "${api.product-composite.create-composite-product.description}", description = "${api.product-composite.create-composite-product.notes}")
  @ApiResponses({
      @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
      @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @PostMapping(value = "/product-composite", consumes = "application/json")
  void createProduct(@RequestBody ProductAggregate body);

  @Operation(summary = "${api.product-composite.get-composite-service.description}", description = "${api.product-composite.get-composite-service.notes}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
      @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
      @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
      @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @GetMapping(value = "/product-composite/{productId}", produces = "application/json")
  ProductAggregate getProduct(@PathVariable int productId);

  @Operation(summary = "${api.product-composite.delete-composite-product.description}", description = "${api.product-composite.delete-composite-product.notes}")
  @ApiResponses({
      @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
      @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
  })
  @DeleteMapping(value = "/product-composite/{productId}")
  void deleteProduct(@PathVariable int productId);
}
