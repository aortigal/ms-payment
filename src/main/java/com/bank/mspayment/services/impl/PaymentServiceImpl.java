package com.bank.mspayment.services.impl;

import com.bank.mspayment.handler.ResponseHandler;
import com.bank.mspayment.models.dao.PaymentDao;
import com.bank.mspayment.models.documents.Payment;
import com.bank.mspayment.services.ActiveService;
import com.bank.mspayment.services.ClientService;
import com.bank.mspayment.services.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentDao dao;
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Autowired
    private ActiveService activeService;

    @Autowired
    private ClientService clientService;

    @Override
    public Mono<ResponseHandler> findAll() {
        log.info("[INI] findAll Payment");
        return dao.findAll()
                .doOnNext(payment -> log.info(payment.toString()))
                .collectList()
                .map(payments -> new ResponseHandler("Done", HttpStatus.OK, payments))
                .onErrorResume(error -> Mono.just(new ResponseHandler(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(new ResponseHandler("No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] findAll Payment"));
    }

    @Override
    public Mono<ResponseHandler> find(String id) {
        log.info("[INI] find Payment");
        return dao.findById(id)
                .doOnNext(payment -> log.info(payment.toString()))
                .map(payment -> new ResponseHandler("Done", HttpStatus.OK, payment))
                .onErrorResume(error -> Mono.just(new ResponseHandler(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(new ResponseHandler("No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] find Payment"));
    }

    @Override
    public Mono<ResponseHandler> create(String type,Payment pay) {
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
                        return Mono.just(new ResponseHandler("Does not have active", HttpStatus.BAD_REQUEST, null));
                    }

                    return clientService.findByCode(pay.getClientId())
                            .doOnNext(transaction -> log.info(transaction.toString()))
                            .flatMap(responseClient -> {
                                if(responseClient.getData() == null){
                                    return Mono.just(new ResponseHandler("Does not have client", HttpStatus.BAD_REQUEST, null));
                                }

                                if(!finalTypeName.equals(responseClient.getData().getType())){
                                    return Mono.just(new ResponseHandler("The Active is not enabled for the client", HttpStatus.BAD_REQUEST, null));
                                }
                                pay.setDateRegister(LocalDateTime.now());
                                return dao.save(pay)
                                        .doOnNext(transaction -> log.info(transaction.toString()))
                                        .map(transaction -> new ResponseHandler("Done", HttpStatus.OK, transaction)                )
                                        .onErrorResume(error -> Mono.just(new ResponseHandler(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                                        ;
                            })
                            .switchIfEmpty(Mono.just(new ResponseHandler("Client No Content", HttpStatus.BAD_REQUEST, null)));

                })
                .switchIfEmpty(Mono.just(new ResponseHandler("Active No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] create Transaction"));

    }

    @Override
    public Mono<ResponseHandler> update(String id,Payment pay) {
        log.info("[INI] update Payment");
        return dao.existsById(id).flatMap(check -> {
            if (check){
                pay.setDateUpdate(LocalDateTime.now());
                return dao.save(pay)
                        .doOnNext(payment -> log.info(payment.toString()))
                        .map(payment -> new ResponseHandler("Done", HttpStatus.OK, payment)                )
                        .onErrorResume(error -> Mono.just(new ResponseHandler(error.getMessage(), HttpStatus.BAD_REQUEST, null)));
            }
            else
                return Mono.just(new ResponseHandler("Not found", HttpStatus.NOT_FOUND, null));

        }).doFinally(fin -> log.info("[END] update Payment"));
    }

    @Override
    public Mono<ResponseHandler> delete(String id) {
        log.info("[INI] delete Payment");
        log.info(id);

        return dao.existsById(id).flatMap(check -> {
            if (check)
                return dao.deleteById(id).then(Mono.just(new ResponseHandler("Done", HttpStatus.OK, null)));
            else
                return Mono.just(new ResponseHandler("Not found", HttpStatus.NOT_FOUND, null));
        }).doFinally(fin -> log.info("[END] delete Payment"));
    }

    @Override
    public Mono<ResponseHandler> findByIdClient(String idClient) {
        log.info("[INI] findByIdClient Payment");
        return dao.findAll()
                .filter(payment ->
                        payment.getClientId().equals(idClient)
                )
                .collectList()
                .doOnNext(transaction -> log.info(transaction.toString()))
                .map(movements -> new ResponseHandler("Done", HttpStatus.OK, movements))
                .onErrorResume(error -> Mono.just(new ResponseHandler(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(new ResponseHandler("No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] findByIdClient Payment"));
    }

    @Override
    public Mono<ResponseHandler> getBalance(String idClient) {
        log.info("[INI] getBalance Payment");
        log.info(idClient);
        AtomicReference<Float> balance = new AtomicReference<>((float) 0);
        return dao.findAll()
                .doOnNext(payment -> {
                    if(payment.getClientId().equals(idClient)) {
                        balance.set(balance.get() + payment.getAmount());
                        log.info(payment.toString());
                    }
                })
                .collectList()
                .map(movements -> new ResponseHandler("Done", HttpStatus.OK, balance.get()))
                .onErrorResume(error -> Mono.just(new ResponseHandler(error.getMessage(), HttpStatus.BAD_REQUEST, null)))
                .switchIfEmpty(Mono.just(new ResponseHandler("No Content", HttpStatus.BAD_REQUEST, null)))
                .doFinally(fin -> log.info("[END] getBalance Payment"));
    }

}
