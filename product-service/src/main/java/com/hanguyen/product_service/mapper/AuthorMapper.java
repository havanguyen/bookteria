package com.hanguyen.product_service.mapper;

import com.hanguyen.product_service.dto.request.AuthorRequest;
import com.hanguyen.product_service.dto.response.AuthorResponse;
import com.hanguyen.product_service.entity.Author;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    Author toAuthor(AuthorRequest request);
    AuthorResponse toAuthorResponse(Author author);
}