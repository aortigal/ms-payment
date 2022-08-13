package com.bank.mspayment.models.dao;

import com.bank.mspayment.models.documents.Payment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PaymentDao extends ReactiveMongoRepository<Payment, String> {
}
