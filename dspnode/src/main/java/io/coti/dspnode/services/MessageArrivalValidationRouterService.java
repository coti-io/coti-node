package io.coti.dspnode.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.DataHash;
import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.dspnode.data.RouterData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageArrivalValidationRouterService {

    @Autowired
    private MessageArrivalValidationCrypto messageArrivalValidationCrypto;

    public MessageArrivalValidationData getMissedMessageHashes(MessageArrivalValidationData data){
        if(!validateResponseSignature(data)){
            return new MessageArrivalValidationData();
        }
        MessageArrivalValidationData missingHashesMessageArrivalValidation = new MessageArrivalValidationData();

        data.getClassNameToHashes().keySet().
                forEach(k ->
                        missingHashesMessageArrivalValidation.addHashesByNewKey((String)k,extractMissedDataHashes((String)k,(Set<? extends DataHash>) data.getClassNameToHashes().get(k))));

        messageArrivalValidationCrypto.signMessage(missingHashesMessageArrivalValidation);
        presentMissingHashesSummary(missingHashesMessageArrivalValidation);
        return missingHashesMessageArrivalValidation;
    }

    private void presentMissingHashesSummary(MessageArrivalValidationData messageArrivalValidationData){
        messageArrivalValidationData.getClassNameToHashes().keySet().forEach(key ->
                ((Set<? extends DataHash>)messageArrivalValidationData.getClassNameToHashes().get(key)).forEach(dataHash ->
                        log.info("Missed hash {} of type {}", dataHash.getHash(), key)));
    }


    private Set<? extends DataHash> extractMissedDataHashes(String key, Set<? extends DataHash> receivedDataHashes){
        return receivedDataHashes.stream().
                map(dataHash -> {
                    log.info("going over hash: {}",dataHash.toString());
                    return RouterData.valueOf(key).getDataCollection().exists(dataHash.getHash()) ? null : dataHash;
                }).
                filter(Objects::nonNull).
                collect(Collectors.toSet());
    }

    public boolean validateResponseSignature(MessageArrivalValidationData message){
        boolean verified = messageArrivalValidationCrypto.verifySignature(message);
        if(!verified){
            //TODO 3/24/2019 astolia: log message with the target that failed.
            log.warn("Failed to authenticate message");
        }
        return verified;
    }
    private void removeVerifiedHashesFromDB(MessageArrivalValidationData data, List<MessageArrivalValidationData> responses) {
        Set<? extends DataHash> allHashes = new HashSet<>();
        // TODO - implelment
    }

}
