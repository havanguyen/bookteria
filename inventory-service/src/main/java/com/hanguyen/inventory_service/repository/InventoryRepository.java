package com.hanguyen.inventory_service.repository;


import com.hanguyen.inventory_service.entity.InventoryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends MongoRepository<InventoryDocument , String> {
    Optional<InventoryDocument> findByBookId(String bookId);
    void deleteByBookId(String bookId);
}
