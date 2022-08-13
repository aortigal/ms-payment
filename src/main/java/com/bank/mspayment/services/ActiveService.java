package com.bank.mspayment.services;

import com.bank.mspayment.models.utils.ResponseActive;
import reactor.core.publisher.Mono;

public interface ActiveService {
    Mono<ResponseActive> findByCode(String id);
}
