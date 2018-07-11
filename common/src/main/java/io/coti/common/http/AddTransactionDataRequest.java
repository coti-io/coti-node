package io.coti.common.http;

import io.coti.common.data.TransactionData;

import javax.validation.constraints.NotNull;

public class AddTransactionDataRequest extends Request {
    @NotNull
    public TransactionData transactionData;
}