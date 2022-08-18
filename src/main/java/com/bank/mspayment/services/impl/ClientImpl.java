package com.bank.mspayment.services.impl;

import com.bank.mspayment.models.utils.ResponseClient;
import com.bank.mspayment.services.ClientService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ClientImpl implements ClientService {

    private final WebClient webClient;

    public ClientImpl(WebClient.Builder webClientBuilder){
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    @Override
    public Mono<ResponseClient> findByCode(String id)
    {
        return webClient.get()
                .uri("/api/client/"+ id)
                .retrieve()
                .bodyToMono(ResponseClient.class);
    }
}
