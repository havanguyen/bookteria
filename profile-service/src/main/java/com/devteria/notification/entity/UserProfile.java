package com.devteria.notification.entity;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Node("user_profile")
public class UserProfile {
    @Id
    @GeneratedValue(generatorClass = GeneratedValue.UUIDGenerator.class)
    UUID id;

    @Property("user_id")
    String userId;

    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
