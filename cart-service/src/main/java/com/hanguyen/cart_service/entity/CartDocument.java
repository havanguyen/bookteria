package com.hanguyen.cart_service.entity;


import com.hanguyen.cart_service.dto.ProductInCart;
import com.hanguyen.cart_service.dto.request.CartRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "cart")
public class CartDocument {
    @Id
    String id;

    @Indexed(unique = true)
    String userId;

    List<ProductInCart> productInCarts;
}
