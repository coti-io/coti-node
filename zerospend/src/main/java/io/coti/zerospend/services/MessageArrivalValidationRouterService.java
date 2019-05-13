package io.coti.zerospend.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.DataHash;
import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.zerospend.data.RouterData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageArrivalValidationRouterService {

    @Autowired
    MessageArrivalValidationCrypto messageArrivalValidationCrypto;

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
            log.warn("Failed to authenticate message with signature: {} and signer hash: {}", message.getSignerHash().toString(), message.getSignature().toString());
        }
        return verified;
    }

    private void presentMissingHashesSummary(MessageArrivalValidationData messageArrivalValidationData){
        messageArrivalValidationData.getClassNameToHashes().keySet().forEach(key ->
                ((Set<? extends DataHash>)messageArrivalValidationData.getClassNameToHashes().get(key)).forEach(dataHash ->
                        log.info("Missed hash {} of type {}", dataHash.getHash(), key)));
    }
}
