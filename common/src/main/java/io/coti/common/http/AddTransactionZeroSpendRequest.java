package io.coti.common.http;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.Request;
import lombok.Data;

@Data
public class AddTransactionZeroSpendRequest extends Request {

    private Hash cotiNodeHash;

    private TransactionData transactionData;

}

