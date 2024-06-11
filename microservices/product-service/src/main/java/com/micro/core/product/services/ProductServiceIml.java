package com.micro.core.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.micro.api.core.product.Product;
import com.micro.api.core.product.ProductService;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.api.exceptions.NotFoundException;
import com.micro.util.http.ServiceUtil;

@RestController
public class ProductServiceIml implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceIml.class);

    private final ServiceUtil serviceUtil;

    public ProductServiceIml(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {
        LOG.debug("/product return product for productId: " + productId);

        if (productId < 1) {
            throw new InvalidInputException("Invalid product Id: " + productId);
        }

        if (productId == 13) {
            throw new NotFoundException("Not found product Id: " + productId);
        }
        return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
    }

}
