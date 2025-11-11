package com.hanguyen.product_service.mapper;

import com.hanguyen.product_service.dto.event.ProductEvent;
import com.hanguyen.product_service.dto.request.ProductRequest;
import com.hanguyen.product_service.dto.response.ProductResponse;
import com.hanguyen.product_service.entity.Category;
import com.hanguyen.product_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        uses = {AuthorMapper.class, CategoryMapper.class, PublisherMapper.class}
)
public interface ProductMapper {

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "publisher", ignore = true)
    Product toProduct(ProductRequest request);


    ProductResponse toProductResponse(Product product);

    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "publisher.name", target = "publisherName")
    @Mapping(source = "categories", target = "categoryNames", qualifiedByName = "categoriesToNames")
    ProductEvent toProductEvent(Product product);

    @Named("categoriesToNames")
    default List<String> categoriesToNames(Set<Category> categories) {
        if (categories == null) {
            return null;
        }
        return categories.stream()
                .map(Category::getName)
                .collect(Collectors.toList());
    }
}