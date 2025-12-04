package com.hanguyen.order_service.repository;


import com.hanguyen.order_service.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders , String> {
    @Query("SELECT o FROM Orders o LEFT JOIN FETCH o.items WHERE o.userId = :userId")
    List<Orders> findAllByUserId(String userId);
}
