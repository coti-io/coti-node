package io.coti.fullnode.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.Collection;
import io.coti.basenode.model.TransactionDataHashes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class MessageArrivalValidationService {

    @Autowired
    private MessageArrivalValidationCrypto messageArrivalValidationCrypto;

    @Autowired
    private NetworkService networkService;

    @Autowired
    private TransactionDataHashes transactionDataHashes;

    @Autowired
    private AddressDataHashes addressDataHashes;

    private Map<String, Collection> classNameToHandlerService;

    @PostConstruct
    private void init(){
        classNameToHandlerService = new HashMap<>();
        classNameToHandlerService.put(AddressDataHashes.class.getName(), addressDataHashes);
        classNameToHandlerService.put(TransactionDataHashes.class.getName(), transactionDataHashes);
    }

    public void addTransactionHash(Hash hash){
        transactionDataHashes.put(new TransactionDataHash(hash));
    }

    public void addAddressHash(Hash hash){
        addressDataHashes.put(new AddressDataHash(hash));
    }

    @Scheduled(fixedDelay = 5000L, initialDelay = 1000L)
    public void checkDspReceivedMessages(){

        log.info("Scheduled task checkDspReceivedMessages");
        Set<TransactionDataHash> transactionHashes = transactionDataHashes.getHashes();
        Set<AddressDataHash> addressHashes = addressDataHashes.getHashes();

        // Avoid sending empty message and unnecessary checks
        if(transactionHashes.isEmpty() && addressHashes.isEmpty()){
            return;
        }
        Map<String, Set<? extends DataHash>> classNameToHashes = new HashMap<>();
        classNameToHashes.put(TransactionDataHashes.class.getName(), transactionDataHashes.getHashes());
        classNameToHashes.put(AddressDataHashes.class.getName(), addressDataHashes.getHashes());
        MessageArrivalValidationData data = new MessageArrivalValidationData(classNameToHashes);
        signAndSend(data);
    }

    private void signAndSend(MessageArrivalValidationData data){
        messageArrivalValidationCrypto.signMessage(data);
        List<MessageArrivalValidationData> responses = networkService.sendDataToConnectedDspsByHttp(data);
        responses.forEach(this::printUnReceivedHashes);
        removeVerifiedHashesFromDB();
    }

    void printUnReceivedHashes(MessageArrivalValidationData response){
        response.getClassNameToHashes().keySet().forEach(key ->
                ((Set<? extends DataHash>)response.getClassNameToHashes().get(key)).forEach(dataHash ->
                    log.info("Missed hash {} of type {}", dataHash.getHash(), key)));
    }

    private void removeVerifiedHashesFromDB() {
        // TODO - implelment
    }

}
