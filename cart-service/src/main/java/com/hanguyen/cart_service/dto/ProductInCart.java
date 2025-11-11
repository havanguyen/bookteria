package com.hanguyen.cart_service.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductInCart implements Serializable {
    @NotEmpty
    String bookId;

    @Min(1)
    int quantity;

    Instant instant;
    boolean isPaid;
}
