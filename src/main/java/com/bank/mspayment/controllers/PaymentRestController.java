package com.bank.mspayment.controllers;

import com.bank.mspayment.handler.ResponseHandler;
import com.bank.mspayment.models.documents.Payment;
import com.bank.mspayment.services.PaymentService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController
{
    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public Mono<ResponseHandler> findAll() {
        return paymentService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseHandler> find(@PathVariable String id) {
        return paymentService.find(id);
    }

    @PostMapping("{type}")
    @CircuitBreaker(name="active", fallbackMethod = "fallBackActive")
    public Mono<ResponseHandler> create(@PathVariable("type") String type, @Valid @RequestBody Payment pay) {
        return paymentService.create(type, pay);

    }

    @PutMapping("/{id}")
    public Mono<ResponseHandler> update(@PathVariable("id") String id, @RequestBody Payment pay) {
        return paymentService.update(id, pay);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseHandler> delete(@PathVariable("id") String id) {
        return paymentService.delete(id);
    }

    @GetMapping("/clientPayments/{idClient}")
    public Mono<ResponseHandler> findByIdClient(@PathVariable String idClient) {
        return paymentService.findByIdClient(idClient);
    }

    @GetMapping("/balance/{idClient}")
    public Mono<ResponseHandler> getBalance(@PathVariable("idClient") String idClient) {
        return paymentService.getBalance(idClient);
    }

    public Mono<ResponseHandler> fallBackActive(RuntimeException runtimeException){
        return Mono.just(new ResponseHandler("Microservicio externo no responde", HttpStatus.BAD_REQUEST,runtimeException.getMessage()));
    }
}
