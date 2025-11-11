package com.hanguyen.cart_service.dto.event;

import com.hanguyen.cart_service.dto.ProductInCart;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartSyncDataEvent implements Serializable {
    String userId;
    ProductInCart productInCart;
}
