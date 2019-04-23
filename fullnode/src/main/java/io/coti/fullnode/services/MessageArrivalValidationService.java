package io.coti.fullnode.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.Collection;
import io.coti.basenode.model.TransactionDataHashes;
import io.coti.basenode.services.interfaces.BaseNodeMessageArrivalValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class MessageArrivalValidationService extends BaseNodeMessageArrivalValidationService {

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

    //TODO 3/24/2019 astolia: change to 10 minutes
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
        responses = validateMessages(responses);
        if(responses.isEmpty()){
            log.error("All responses failed signature validation!");
            return;
        }
        responses.forEach(this::printUnreceivedHashes);
        removeVerifiedHashesFromDB(data, responses);
    }

    //TODO 3/24/2019 astolia: make sure works
    private List<MessageArrivalValidationData> validateMessages(List<MessageArrivalValidationData> responses){
        responses.removeIf(response -> verifyAndLogSingleMessageArrivalValidation(response));
        return responses;

    }

    public boolean verifyAndLogSingleMessageArrivalValidation(MessageArrivalValidationData response){
        boolean verified = messageArrivalValidationCrypto.verifySignature(response);
        if(!verified){
            //TODO 3/24/2019 astolia: log message with the tarfet that failed.
            log.warn("Failed to authenticate message");
        }
        return verified;
    }

    void printUnreceivedHashes(MessageArrivalValidationData response){
        response.getClassNameToHashes().keySet().forEach(key ->
                ((Set<? extends DataHash>)response.getClassNameToHashes().get(key)).forEach(dataHash ->
                    log.info("Missed hash {} of type {}", dataHash.getHash(), key)));
    }

    private void removeVerifiedHashesFromDB(MessageArrivalValidationData data, List<MessageArrivalValidationData> responses) {
        Set<? extends DataHash> allHashes = new HashSet<>();
        // TODO - implelment
    }

}
