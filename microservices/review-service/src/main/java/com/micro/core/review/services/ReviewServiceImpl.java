package com.micro.core.review.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import com.micro.api.core.review.Review;
import com.micro.api.core.review.ReviewService;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.util.http.ServiceUtil;

@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;

    public ReviewServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid product Id: " + productId);
        }

        if (productId == 213) {
            LOG.debug("No review found for productId: " + productId);
            return new ArrayList<>();
        }

        List<Review> list = new ArrayList<>();
        list.add(new Review(productId, 1, "Author 1", "content 1", serviceUtil.getServiceAddress()));
        list.add(new Review(productId, 2, "Author 2", "content 2", serviceUtil.getServiceAddress()));
        list.add(new Review(productId, 3, "Author 3", "content 3", serviceUtil.getServiceAddress()));

        return list;
    }
}
