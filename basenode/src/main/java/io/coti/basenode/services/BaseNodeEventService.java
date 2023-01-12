package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.http.GetTransactionResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.IEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.services.BaseNodeServiceManager.nodeTransactionHelper;
import static io.coti.basenode.services.BaseNodeServiceManager.transactions;

@Slf4j
@Service
public class BaseNodeEventService implements IEventService {

    protected Map<Event, Hash> eventsMap = new ConcurrentHashMap<>();

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
        EventInputBaseTransactionData eventInputBaseTransactionData = nodeTransactionHelper.getEventInputBaseTransactionData(eventTransactionData);
        Event event = eventInputBaseTransactionData.getEvent();
        boolean isHardFork = event.isHardFork();
        if (eventsMap.containsKey(event) && isHardFork) {
            log.error(String.format("Event of %s already processed as Hard Fork Event", eventInputBaseTransactionData.getEvent()));
            return false;
        }
        eventsMap.put(event, eventTransactionData.getHash());
        return true;
    }

    public TransactionData getEventTransactionData(Event event) {
        Hash eventTransactionHash = eventsMap.get(event);
        TransactionData eventTransactionData = null;
        if (eventTransactionHash != null) {
            eventTransactionData = transactions.getByHash(eventTransactionHash);
        }
        return eventTransactionData;
    }

    public boolean eventHappened(Event event) {
        return getConfirmedEventTransactionData(event) != null;
    }

    public TransactionData getConfirmedEventTransactionData(Event event) {
        TransactionData eventTransactionData = getEventTransactionData(event);
        if (eventTransactionData != null && nodeTransactionHelper.isConfirmed(eventTransactionData)) {
            return eventTransactionData;
        }
        return null;
    }

    public ResponseEntity<IResponse> getEventTransactionDataResponse(Event event) {
        TransactionData transactionData = getEventTransactionData(event);
        return prepareResponse(transactionData);
    }

    public ResponseEntity<IResponse> getConfirmedEventTransactionDataResponse(Event event) {
        TransactionData confirmedTransactionData = getConfirmedEventTransactionData(event);
        return prepareResponse(confirmedTransactionData);
    }

    private ResponseEntity<IResponse> prepareResponse(TransactionData transactionData) {
        if (transactionData != null) {
            return ResponseEntity.status(HttpStatus.OK).body(new GetTransactionResponse(new TransactionResponseData(transactionData)));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response("Event Not Found", STATUS_ERROR));
        }
    }
}
