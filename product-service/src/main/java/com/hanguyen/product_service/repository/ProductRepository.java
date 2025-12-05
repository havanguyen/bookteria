package com.hanguyen.product_service.repository;

import com.hanguyen.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @EntityGraph(attributePaths = {"author", "publisher", "categories"})
    @Query("SELECT p FROM Product p")
    Page<Product> findAllWithRelations(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "publisher", "categories"})
    List<Product> findAllByAuthorId(String authorId);

    @EntityGraph(attributePaths = {"author", "publisher", "categories"})
    List<Product> findAllByPublisherId(String publisherId);

    @EntityGraph(attributePaths = {"author", "publisher", "categories"})
    List<Product> findAllByCategories_Id(String categoryId);
}