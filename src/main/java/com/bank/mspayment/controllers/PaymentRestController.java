package com.bank.mspayment.controllers;

import com.bank.mspayment.handler.ResponseHandler;
import com.bank.mspayment.models.dao.PaymentDao;
import com.bank.mspayment.models.documents.Payment;
import com.bank.mspayment.services.ActiveService;
import com.bank.mspayment.services.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController
{
    @Autowired
    private PaymentDao dao;
    private static final Logger log = LoggerFactory.getLogger(PaymentRestController.class);

    @Autowired
    private ActiveService activeService;

    @Autowired
    private ClientService clientService;

    @GetMapping
    public Mono<ResponseEntity<Object>> findAll()
    {
        log.info("[INI] findAll Payment");
        return dao.findAll()
                .doOnNext(payment -> log.info(payment.toString()))
                .collectList()
                .map(payments -> ResponseHandler.response("Done", HttpStatus.OK, payments))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] findAll Payment"));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> find(@PathVariable String id)
    {
        log.info("[INI] find Payment");
        return dao.findById(id)
                .doOnNext(payment -> log.info(payment.toString()))
                .map(payment -> ResponseHandler.response("Done", HttpStatus.OK, payment))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] find Payment"));
    }

    @PostMapping("{type}")
    public Mono<ResponseEntity<Object>> create(@PathVariable("type") String type, @Valid @RequestBody Payment pay)
    {
        log.info("[INI] create payment");

        String typeName = "";
        if(type.equals("1")){
            typeName = "PERSONAL";
        }else if(type.equals("2")){
            typeName = "COMPANY";
        }

        String finalTypeName = typeName;
        return activeService.findByCode(pay.getActiveId())
                .doOnNext(payment -> log.info(payment.toString())).
                flatMap(responseActive -> {
                    if(responseActive.getData()==null){
                        return Mono.just(ResponseHandler.response("Does not have active", HttpStatus.BAD_REQUEST, null));
                    }

                    return clientService.findByCode(pay.getClientId())
                            .doOnNext(transaction -> log.info(transaction.toString()))
                            .flatMap(responseClient -> {
                                if(responseClient.getData() == null){
                                    return Mono.just(ResponseHandler.response("Does not have client", HttpStatus.BAD_REQUEST, null));
                                }

                                if(!finalTypeName.equals(responseClient.getData().getType())){
                                    return Mono.just(ResponseHandler.response("The Active is not enabled for the client", HttpStatus.BAD_REQUEST, null));
                                }
                                pay.setDateRegister(LocalDateTime.now());
                                return dao.save(pay)
                                        .doOnNext(transaction -> log.info(transaction.toString()))
                                        .map(transaction -> ResponseHandler.response("Done", HttpStatus.OK, transaction)                )
                                        .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                                        ;
                            })
                            .switchIfEmpty(Mono.just(ResponseHandler.response("Client No Content", HttpStatus.BAD_REQUEST, null)));

                })
                .switchIfEmpty(Mono.just(ResponseHandler.response("Active No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] create Transaction"));

    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> update(@PathVariable("id") String id, @RequestBody Payment pay)
    {
        log.info("[INI] update Payment");
        return dao.existsById(id).flatMap(check -> {
            if (check){
                pay.setDateUpdate(LocalDateTime.now());
                return dao.save(pay)
                        .doOnNext(payment -> log.info(payment.toString()))
                        .map(payment -> ResponseHandler.response("Done", HttpStatus.OK, payment)                )
                        .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
            }
            else
                return Mono.just(ResponseHandler.response("Not found", HttpStatus.NOT_FOUND, null));

        }).doFinally(fin -> log.info("[END] update Payment"));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Object>> delete(@PathVariable("id") String id)
    {
        log.info("[INI] delete Payment");
        log.info(id);

        return dao.existsById(id).flatMap(check -> {
            if (check)
                return dao.deleteById(id).then(Mono.just(ResponseHandler.response("Done", HttpStatus.OK, null)));
            else
                return Mono.just(ResponseHandler.response("Not found", HttpStatus.NOT_FOUND, null));
        }).doFinally(fin -> log.info("[END] delete Payment"));
    }

    @GetMapping("/clientPayments/{idClient}")
    public Mono<ResponseEntity<Object>> findByIdClient(@PathVariable String idClient)
    {
        log.info("[INI] findByIdClient Payment");
        return dao.findAll()
                .filter(payment ->
                        payment.getClientId().equals(idClient)
                )
                .collectList()
                .doOnNext(transaction -> log.info(transaction.toString()))
                .map(movements -> ResponseHandler.response("Done", HttpStatus.OK, movements))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] findByIdClient Payment"));
    }

    @GetMapping("/balance/{idClient}")
    public Mono<ResponseEntity<Object>> getBalance(@PathVariable("idClient") String idClient)
    {
        log.info("[INI] getBalance Payment");
        log.info(idClient);
        AtomicReference<Float> balance = new AtomicReference<>((float) 0);
        return dao.findAll()
                .doOnNext(payment -> {
                    if(payment.getClientId().equals(idClient)) {
                        balance.set(balance.get() + payment.getMont());
                        log.info(payment.toString());
                    }
                })
                .collectList()
                .map(movements -> ResponseHandler.response("Done", HttpStatus.OK, balance.get()))
                .onErrorResume(error -> Mono.just(ResponseHandler.response(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(ResponseHandler.response("No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] getBalance Payment"));
    }
}
