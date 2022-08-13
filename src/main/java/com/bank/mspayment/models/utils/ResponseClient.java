package com.bank.mspayment.models.utils;

import com.bank.mspayment.models.documents.Client;
import lombok.Data;

@Data
public class ResponseClient
{
    private Client data;

    private String message;

    private String status;

}
