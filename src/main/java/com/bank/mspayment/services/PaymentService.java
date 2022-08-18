package com.bank.mspayment.services;

import com.bank.mspayment.handler.ResponseHandler;
import com.bank.mspayment.models.documents.Payment;
import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<ResponseHandler> findAll();

    Mono<ResponseHandler> find(String id);

    Mono<ResponseHandler> create(String type, Payment pay);

    Mono<ResponseHandler> update(String id,Payment pay);

    Mono<ResponseHandler> delete(String id);

    Mono<ResponseHandler> findByIdClient(String idClient);

    Mono<ResponseHandler> getBalance(String idClient);
}
