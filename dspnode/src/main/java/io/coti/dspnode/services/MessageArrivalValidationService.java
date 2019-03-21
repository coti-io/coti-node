package io.coti.dspnode.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.Collection;
import io.coti.basenode.model.TransactionDataHashes;
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
public class MessageArrivalValidationService {

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
        MessageArrivalValidationData missingHashesMessageArrivalValidation = new MessageArrivalValidationData();

        data.getClassNameToHashes().keySet().
                forEach(k ->
                        missingHashesMessageArrivalValidation.addHashesByNewKey((String)k,extractMissedDataHashes((String)k,(Set<? extends DataHash>) data.getClassNameToHashes().get(k))));

        messageArrivalValidationCrypto.signMessage(missingHashesMessageArrivalValidation);
        presentSummery(missingHashesMessageArrivalValidation);
        return missingHashesMessageArrivalValidation;
    }

    private void presentSummery(MessageArrivalValidationData messageArrivalValidationData){
        messageArrivalValidationData.getClassNameToHashes().keySet().forEach(k -> printAllMissingHashes((String)k,(Set<? extends DataHash>)messageArrivalValidationData.getClassNameToHashes().get(k)));
    }

    private void printAllMissingHashes(String key, Set<? extends DataHash> values){
        log.info("Missed {} messages: {}", key, values);
    }

    private Set<? extends DataHash> extractMissedDataHashes(String key, Set<? extends DataHash> receivedDataHashes){
        return receivedDataHashes.stream().
                map(dataHash -> classNameToHandlerService.get(key).exists(dataHash.getHash()) ? null : dataHash).
                filter(Objects::nonNull).
                collect(Collectors.toSet());
    }

//    @Scheduled(fixedDelay = 5000L, initialDelay = 1000L)
    public void checkSentMessagesReceivedByDestinationNode(){
        //TODO 3/20/2019 : implement flow 2 ( Checking of messages sent by DSP to FN)
    }

}
