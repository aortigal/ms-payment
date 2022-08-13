package com.bank.mspayment.models.utils;

import com.bank.mspayment.models.documents.Active;
import lombok.Data;

@Data
public class ResponseActive
{
    private Active data;

    private String message;

    private String status;

}
