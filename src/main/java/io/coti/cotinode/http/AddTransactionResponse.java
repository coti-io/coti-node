package io.coti.cotinode.http;


import io.coti.cotinode.data.Hash;

import static io.coti.cotinode.http.HttpStringConstants.TRANSACTION_CREATED_MESSAGE;

public class AddTransactionResponse extends Response {

        public AddTransactionResponse(Hash transactionHash){
            super(String.format(TRANSACTION_CREATED_MESSAGE, transactionHash));
        }
}
