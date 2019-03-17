package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.basenode.model.UnvalidatedArrivalMessageHashes;
import io.coti.basenode.services.interfaces.IMessageArrivalValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public abstract class BaseNodeMessageArrivalValidationService implements IMessageArrivalValidationService {

    @Autowired
    private MessageArrivalValidationCrypto messageArrivalValidationCrypto;

    @Autowired
    private UnvalidatedArrivalMessageHashes unvalidatedArrivalMessageHashes;

    @Autowired
    private ISender sender;

    @Autowired
    private IReceiver receiver;

    private Hash lastHash;


    public void addHash(Hash hash){
        MessageArrivalValidationData data = unvalidatedArrivalMessageHashes.getByHash(lastHash);
        if(data != null) {
            data.addHashToUnvalidatedArrivalMessageHashes(hash);
        }
        else{
            lastHash = hash;
            data = new MessageArrivalValidationData(hash);
        }
        unvalidatedArrivalMessageHashes.put(data);
    }

    private void sendUnvalidatedMessageArrivalHashes(){

    }

    @Scheduled(fixedDelay = 5000L)
    private void sentMessagesCheck(){
//        sender.send();
    }


}
