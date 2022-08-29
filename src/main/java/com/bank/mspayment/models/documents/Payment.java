package com.bank.mspayment.models.documents;

import com.bank.mspayment.models.utils.Audit;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Data
@Document(collection = "payments")
public class Payment extends Audit
{
    @Id
    private String id;
    @NotNull(message = "activeId must not be null")
    private String activeId;
    @NotNull(message = "clientId must not be null")
    private String clientId;
    @NotNull(message = "creditId must not be null")
    private String creditId;
    @NotNull(message = "amount must not be null")
    private Float amount;
}
