package io.coti.dspnode.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.Collection;
import io.coti.basenode.model.TransactionDataHashes;
import io.coti.basenode.services.interfaces.BaseNodeMessageArrivalValidationService;
import io.coti.basenode.services.interfaces.IMessageArrivalValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageArrivalValidationService extends BaseNodeMessageArrivalValidationService {

    @Autowired
    private TransactionDataHashes transactionDataHashes;

    @Autowired
    private AddressDataHashes addressDataHashes;

    @Autowired
    private MessageArrivalValidationCrypto messageArrivalValidationCrypto;

    Map<String, Collection> classNameToHandlerService;

    public void addTransactionHash(Hash hash){
        transactionDataHashes.put(new TransactionDataHash(hash));
    }

    public void addAddressHash(Hash hash){
        addressDataHashes.put(new AddressDataHash(hash));
    }

    @PostConstruct
    private void init(){
        classNameToHandlerService = new HashMap<>();
        classNameToHandlerService.put(AddressDataHashes.class.getName(), addressDataHashes);
        classNameToHandlerService.put(TransactionDataHashes.class.getName(), transactionDataHashes);
    }

    public MessageArrivalValidationData getMissedMessageHashes(MessageArrivalValidationData data){
        if(!verifyAndLogSingleMessageArrivalValidation(data)){
            return null;
        }
        MessageArrivalValidationData missingHashesMessageArrivalValidation = new MessageArrivalValidationData();

        data.getClassNameToHashes().keySet().
                forEach(k ->
                        missingHashesMessageArrivalValidation.addHashesByNewKey((String)k,extractMissedDataHashes((String)k,(Set<? extends DataHash>) data.getClassNameToHashes().get(k))));

        messageArrivalValidationCrypto.signMessage(missingHashesMessageArrivalValidation);
        presentSummary(missingHashesMessageArrivalValidation);
        return missingHashesMessageArrivalValidation;
    }

    private void presentSummary(MessageArrivalValidationData messageArrivalValidationData){
        messageArrivalValidationData.getClassNameToHashes().keySet().forEach(key ->
                ((Set<? extends DataHash>)messageArrivalValidationData.getClassNameToHashes().get(key)).forEach(dataHash ->
                    log.info("Missed hash {} of type {}", dataHash.getHash(), key)));
    }


    private Set<? extends DataHash> extractMissedDataHashes(String key, Set<? extends DataHash> receivedDataHashes){
        return receivedDataHashes.stream().
                map(dataHash -> classNameToHandlerService.get(key).exists(dataHash.getHash()) ? null : dataHash).
                filter(Objects::nonNull).
                collect(Collectors.toSet());
    }

    @Override
    public boolean verifyAndLogSingleMessageArrivalValidation(MessageArrivalValidationData message){
        boolean verified = messageArrivalValidationCrypto.verifySignature(message);
        if(!verified){
            //TODO 3/24/2019 astolia: log message with the target that failed.
            log.warn("Failed to authenticate message");
        }
        return verified;
    }

//    @Scheduled(fixedDelay = 5000L, initialDelay = 1000L)
    public void checkSentMessagesReceivedByDestinationNode(){
        //TODO 3/20/2019 : implement flow 2 ( Checking of messages sent by DSP to FN)
    }

}
