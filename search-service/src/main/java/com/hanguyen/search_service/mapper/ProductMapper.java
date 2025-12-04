package com.hanguyen.search_service.mapper;

import com.hanguyen.search_service.document.ProductDocument;
import com.hanguyen.search_service.dto.event.ProductEvent;
import com.hanguyen.search_service.dto.reponse.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDocument toProductDocument(ProductEvent productEvent);

    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "publisher.name", target = "publisherName")
    ProductDocument toProductDocument(ProductResponse product);
}