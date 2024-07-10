package com.micro.core.recommendation.services;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.micro.api.core.recommendation.Recommendation;
import com.micro.core.recommendation.persistence.RecommendationEntity;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
  @Mappings({ @Mapping(target = "rate", source = "entity.rating"), @Mapping(target = "serviceAddress", ignore = true) })
  Recommendation entityToApi(RecommendationEntity entity);

  @Mappings({ @Mapping(target = "rating", source = "api.rate"), @Mapping(target = "id", ignore = true),
      @Mapping(target = "version", ignore = true) })
  RecommendationEntity apiToEntity(Recommendation api);

  List<Recommendation> entityListToApiList(List<RecommendationEntity> api);

  List<RecommendationEntity> apiListToEntityList(List<Recommendation> entity);
}
