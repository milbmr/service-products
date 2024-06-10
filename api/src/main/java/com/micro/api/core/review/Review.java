package com.micro.api.core.review;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Review {
    private final int productId;
    private final int reviewId;
    private final String author;
    private final String content;
    private final String serviceAddress;

    public Review() {
        productId = 0;
        reviewId = 0;
        author = null;
        content = null;
        serviceAddress = null;
    }
}