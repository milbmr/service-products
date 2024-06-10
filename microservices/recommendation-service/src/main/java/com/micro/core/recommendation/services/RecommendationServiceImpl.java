package com.micro.core.recommendation.services;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.micro.api.core.recommendation.Recommendation;
import com.micro.api.core.recommendation.RecommendationService;
import com.micro.api.exceptions.InvalidInputException;
import com.micro.util.http.ServiceUtil;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;

    public RecommendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid product id" + productId);
        }

        if (productId == 113) {
            LOG.debug("No recommendations found for productId: " + productId);
            return new ArrayList<Recommendation>();
        }

        List<Recommendation> list = new ArrayList<Recommendation>();
        list.add(new Recommendation(productId, 1, "Author 1", 1, "content 1", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 2, "Author 2", 2, "content 2", serviceUtil.getServiceAddress()));
        list.add(new Recommendation(productId, 3, "Author 3", 3, "content 3", serviceUtil.getServiceAddress()));

        LOG.debug("/recommendation response size {}: " + list.size());

        return list;
    }

}
