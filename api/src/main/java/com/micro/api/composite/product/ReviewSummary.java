package com.micro.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ReviewSummary {
    private final int productId;
    private final String author;
    private final String subject;
}
