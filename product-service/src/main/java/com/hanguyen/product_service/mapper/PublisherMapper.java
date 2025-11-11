package com.hanguyen.product_service.mapper;

import com.hanguyen.product_service.dto.request.PublisherRequest;
import com.hanguyen.product_service.dto.response.PublisherResponse;
import com.hanguyen.product_service.entity.Publisher;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PublisherMapper {
    Publisher toPublisher(PublisherRequest request);
    PublisherResponse toPublisherResponse(Publisher publisher);
}
