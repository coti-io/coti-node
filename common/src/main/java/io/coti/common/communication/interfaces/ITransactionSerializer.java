package io.coti.common.communication.interfaces;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.coti.common.data.TransactionData;

import java.io.IOException;

public interface ITransactionSerializer {

    byte[] serializeTransaction(TransactionData transactionData);

    TransactionData deserializeMessage(byte[] bytes);
}
