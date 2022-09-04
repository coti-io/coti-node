package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Event;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IEventService {

    void init();

    void handleExistingTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    boolean checkEventAndUpdateEventsTable(TransactionData transactionData);

    TransactionData getEventTransactionData(Event event);

    TransactionData getConfirmedEventTransactionData(Event event);

    ResponseEntity<IResponse> getEventTransactionDataResponse(Event event);

    ResponseEntity<IResponse> getConfirmedEventTransactionDataResponse(Event event);

    boolean eventHappened(Event event);

}
