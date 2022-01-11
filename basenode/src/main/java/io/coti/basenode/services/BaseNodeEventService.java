package io.coti.basenode.services;

import io.coti.basenode.data.EventInputBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.http.GetTransactionsResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IEventService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class BaseNodeEventService implements IEventService {

    protected Map<String, TransactionData> eventsMap = new ConcurrentHashMap<>();
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private Transactions transactions;

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void handleExistingTransaction(TransactionData transactionData) {
        checkEventAndUpdateEventsTable(transactionData);
    }

    @Override
    public void handleMissingTransaction(TransactionData transactionData) {
        checkEventAndUpdateEventsTable(transactionData);
    }

    @Override
    public boolean checkEventAndUpdateEventsTable(TransactionData transactionData) {
        if (transactionData.getType().equals(TransactionType.EventHardFork)) {
            return addEventToMap(transactionData);
        }
        return false;
    }

    private synchronized boolean addEventToMap(TransactionData eventTransactionData) {
        EventInputBaseTransactionData event = transactionHelper.getEventInputBaseTransactionData(eventTransactionData);
        String eventDescription = event.getEvent();
        boolean isHardFork = event.isHardFork();
        if (eventsMap.containsKey(eventDescription) && isHardFork) {
            log.error(String.format("Event of %s already processed as Hard Fork Event", event.getEvent()));
            return false;
        }
        eventsMap.put(eventDescription, eventTransactionData);
        return true;
    }

    public TransactionData getEventTransactionData(String event) {
        TransactionData eventTransactionData = eventsMap.get(event);
        if (eventTransactionData != null) {
            eventTransactionData = transactions.getByHash(eventTransactionData.getHash());
        }
        //todo change the value of map to hash id
        return eventTransactionData;
    }

    public TransactionData getConfirmedEventTransactionData(String event) {
        TransactionData eventTransactionData = getEventTransactionData(event);
        if (eventTransactionData != null && transactionHelper.isConfirmed(eventTransactionData)) {
            return eventTransactionData;
        }
        return null;
    }

    public ResponseEntity<IResponse> getEventTransactionDataResponse(String event) {
        TransactionData transactionData = getEventTransactionData(event);
        return prepareResponse(transactionData);
    }

    public ResponseEntity<IResponse> getConfirmedEventTransactionDataResponse(String event) {
        TransactionData confirmedTransactionData = getConfirmedEventTransactionData(event);
        return prepareResponse(confirmedTransactionData);
    }

    private ResponseEntity<IResponse> prepareResponse(TransactionData transactionData) {
        if (transactionData != null) {
            List<TransactionData> transactionsList = new ArrayList<>();
            transactionsList.add(transactionData);
            return ResponseEntity.status(HttpStatus.OK).body(new GetTransactionsResponse(transactionsList));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("Event Not Found", STATUS_ERROR));
        }
    }

    public class EVENTS {
        public static final String MULTI_CURRENCY = "MULTI_CURRENCY";

        EVENTS() {
        }
    }

}
