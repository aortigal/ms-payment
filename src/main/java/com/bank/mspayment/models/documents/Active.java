package com.bank.mspayment.models.documents;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Active
{
    @Id
    private String id;

    private String clientId;
}
