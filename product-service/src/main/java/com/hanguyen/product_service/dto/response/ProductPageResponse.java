package com.hanguyen.product_service.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductPageResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

     List<ProductResponse> content;
     int pageNo;
     int pageSize;
     long totalElements;
     int totalPages;
     boolean last;
}