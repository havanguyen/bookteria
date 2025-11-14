package com.hanguyen.order_service.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.hanguyen.order_service.dto.ShippingAddress;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    OrderStatus orderStatus;
    String userId;

    String ipAddress;

    String paymentUrl;

    Double totalAmount;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    ShippingAddress shippingAddress;


    @Column(columnDefinition = "TEXT")
    String note;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<OrderItem> items;

    @CreationTimestamp
    OffsetDateTime createdAt;

    @UpdateTimestamp
    OffsetDateTime updatedAt;
}
