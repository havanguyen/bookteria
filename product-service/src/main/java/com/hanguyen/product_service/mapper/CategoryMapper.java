package com.hanguyen.product_service.mapper;

import com.hanguyen.product_service.dto.request.CategoryRequest;
import com.hanguyen.product_service.dto.response.CategoryResponse;
import com.hanguyen.product_service.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parentCategory", ignore = true)
    Category toCategory(CategoryRequest categoryRequest);

    CategoryResponse toCategoryResponse(Category category);
}
