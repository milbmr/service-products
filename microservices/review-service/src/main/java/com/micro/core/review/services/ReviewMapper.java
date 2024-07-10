package com.micro.core.review.services;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.micro.api.core.review.Review;
import com.micro.core.review.persistence.ReviewEntity;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

  @Mappings({ @Mapping(target = "serviceAddress", ignore = true) })
  Review entityToApi(ReviewEntity entity);

  @Mappings({ @Mapping(target = "id", ignore = true), @Mapping(target = "version", ignore = true) })
  ReviewEntity apiToEntity(Review review);

  List<Review> entityListToApiList(List<ReviewEntity> entityList);
  List<ReviewEntity> apiListToEntityList(List<Review> apiList);
}
