package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IEventService {

    void init();

    void handleExistingTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    boolean checkEventAndUpdateEventsTable(TransactionData transactionData);

    TransactionData getEventTransactionData(String event);

    TransactionData getConfirmedEventTransactionData(String event);

    ResponseEntity<IResponse> getEventTransactionDataResponse(String event);

    ResponseEntity<IResponse> getConfirmedEventTransactionDataResponse(String event);

}
