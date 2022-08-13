package com.bank.mspayment.services;

import com.bank.mspayment.models.utils.ResponseClient;
import reactor.core.publisher.Mono;

public interface ClientService {
    Mono<ResponseClient> findByCode(String id);
}
