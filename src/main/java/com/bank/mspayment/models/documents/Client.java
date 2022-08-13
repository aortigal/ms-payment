package com.bank.mspayment.models.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;


@Data
public class Client
{
    @Id
    private String id;
    private String type;
    private String firstname;
    private String lastName;
    private String genre;
    private String documentId;
    private String phoneNumber;
    private String email;
}
