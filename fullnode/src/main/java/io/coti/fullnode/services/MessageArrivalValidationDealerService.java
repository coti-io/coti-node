package io.coti.fullnode.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.DataHash;
import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.fullnode.data.DealerData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class MessageArrivalValidationDealerService {

    @Autowired
    private MessageArrivalValidationCrypto messageArrivalValidationCrypto;

    @Autowired
    private NetworkService networkService;

    @Scheduled(fixedDelay = 5000L, initialDelay = 1000L)
    public void validatedMessagesAcceptedByRecipient(){
        log.info("Full node Starting arrival validation flow for sent messages.");
        Map<String, Set<? extends DataHash>> classNameToHashes = new HashMap<>();
        Arrays.stream(DealerData.values()).forEach(data ->
                classNameToHashes.put(data.name(), (data.getDataCollection().getAll())));

        if(classNameToHashes.entrySet().stream().allMatch(entry -> entry.getValue().isEmpty())){
            return;
        }

        MessageArrivalValidationData data = new MessageArrivalValidationData(classNameToHashes);
        handleMessageValidationResponses(signAndSend(data), data);
    }

    private List<MessageArrivalValidationData> signAndSend(MessageArrivalValidationData data) {
        messageArrivalValidationCrypto.signMessage(data);
        return networkService.sendDataToConnectedDspsByHttp(data);
    }

    private void handleMessageValidationResponses(List<MessageArrivalValidationData> responses, MessageArrivalValidationData sentData){
        List<MessageArrivalValidationData> validateResponses = validateResponsesSignatures(responses);
        if(validateResponses.isEmpty()){
            log.warn("No valid response received");
            return;
        }
        validateResponses.forEach(this::printUnreceivedHashes);
        removeVerifiedHashesFromDB(sentData, validateResponses);
    }

    private List<MessageArrivalValidationData> validateResponsesSignatures(List<MessageArrivalValidationData> responses){
        responses.removeIf(this::validateResponseSignature);
        return responses;

    }

    public boolean validateResponseSignature(MessageArrivalValidationData response){
        boolean verified = messageArrivalValidationCrypto.verifySignature(response);
        if(!verified){
            log.warn("Failed to authenticate message ");
        }
        return verified;
    }

    void printUnreceivedHashes(MessageArrivalValidationData response){
        if(!response.isEmpty()){
            response.getClassNameToHashes().forEach((key,value) ->
                ((Set<? extends DataHash>)value).forEach(dataHash ->
                    log.info("Missed hash {} of type {}", dataHash.getHash(), key)));
        }
        else{
            log.info("Response is empty - all messages were received.");
        }
    }

    private void removeVerifiedHashesFromDB(MessageArrivalValidationData data, List<MessageArrivalValidationData> responses) {
        Set<? extends DataHash> allHashes = new HashSet<>();
        // TODO - implelment
    }

}
