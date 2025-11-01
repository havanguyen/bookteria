package com.hanguyen.search_service.mapper;

import com.hanguyen.search_service.document.ProductDocument;
import com.hanguyen.search_service.dto.event.ProductEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDocument toProductDocument(ProductEvent productEvent);
}