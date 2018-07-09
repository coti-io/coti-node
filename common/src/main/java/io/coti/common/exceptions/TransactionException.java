package io.coti.common.exceptions;


import io.coti.common.data.BaseTransactionData;

import java.util.List;

public class TransactionException extends RuntimeException{

    private List<BaseTransactionData> baseTransactionData;

    public TransactionException(){
        super();
    }
    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(Exception ex) {
        super(ex);
    }

    public TransactionException(Exception ex, List<BaseTransactionData> baseTransactionData) {
        super(ex);
        this.baseTransactionData = baseTransactionData;
    }

    public TransactionException(String message, Exception ex) {
        super(message, ex);
    }

    public List<BaseTransactionData> getBaseTransactionData() {
        return baseTransactionData;
    }

}

