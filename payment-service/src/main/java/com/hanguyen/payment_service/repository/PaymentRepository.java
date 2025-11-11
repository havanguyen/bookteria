package com.hanguyen.payment_service.repository;

import com.hanguyen.payment_service.entity.PaymentTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransactions , String> {
    Optional<PaymentTransactions> findByExternalTransactionId(String externalTransactionId);
}
