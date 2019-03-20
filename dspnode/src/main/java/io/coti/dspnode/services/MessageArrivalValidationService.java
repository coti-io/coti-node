package io.coti.dspnode.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.TransactionDataHashes;
import io.coti.basenode.services.BaseNodeMessageArrivalValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void addTransactionHash(Hash hash){
        transactionDataHashes.put(new TransactionDataHash(hash));
    }

    public void addAddressHash(Hash hash){
        addressDataHashes.put(new AddressDataHash(hash));
    }

    public MessageArrivalValidationData getMissedMessageHashes(MessageArrivalValidationData data){
        Set<AddressDataHash> addressHashes = data.getAddressHashes();
        Set<TransactionDataHash> transactionHashes = data.getTransactionHashes();

        MessageArrivalValidationData missingHashesMessageArrivalValidation = new MessageArrivalValidationData();

        missingHashesMessageArrivalValidation.setAddressHashes(
                addressHashes.stream().
                        map(addressDataHash -> addressDataHashes.getByHash(addressDataHash.getHash()) == null ? addressDataHash : null).
                        filter(Objects::nonNull).
                        collect(Collectors.toSet()));

        missingHashesMessageArrivalValidation.setTransactionHashes(
                transactionHashes.stream().
                        map(transactionDataHash -> transactionDataHashes.getByHash(transactionDataHash.getHash()) == null ? transactionDataHash : null).
                        filter(Objects::nonNull).
                        collect(Collectors.toSet()));

        messageArrivalValidationCrypto.signMessage(missingHashesMessageArrivalValidation);
        log.info("Sending missedDataHashes answer: {}",missingHashesMessageArrivalValidation);
        log.info("Missed TransactionData messages: {}, Missed AddressData messages: {}", missingHashesMessageArrivalValidation.getTransactionHashes().size(), missingHashesMessageArrivalValidation.getAddressHashes().size());
        return data;

    }

//    @Scheduled(fixedDelay = 5000L, initialDelay = 1000L)
    public void checkSentMessagesReceivedByDestinationNode(){
        //TODO 3/20/2019 astolia: flow 2
    }

}
