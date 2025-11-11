package com.hanguyen.search_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products") // tuong duong voi entity
public class ProductDocument {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Double)
    private double basePrice;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Text)
    private String authorName;

    @Field(type = FieldType.Text)
    private String publisherName;

    @Field(type = FieldType.Keyword)
    private List<String> categoryNames;
}