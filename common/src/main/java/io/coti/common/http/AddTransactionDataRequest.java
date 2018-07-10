package io.coti.common.http;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;

import javax.validation.constraints.NotNull;
import java.util.List;

public class AddTransactionDataRequest extends Request {
    @NotNull
    public TransactionData transactionData;
}