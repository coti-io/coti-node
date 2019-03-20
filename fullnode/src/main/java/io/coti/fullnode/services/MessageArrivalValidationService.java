package io.coti.fullnode.services;

import io.coti.basenode.crypto.MessageArrivalValidationCrypto;
import io.coti.basenode.data.AddressDataHash;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.MessageArrivalValidationData;
import io.coti.basenode.data.TransactionDataHash;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.TransactionDataHashes;
import io.coti.basenode.services.BaseNodeMessageArrivalValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

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
        MessageArrivalValidationData data = new MessageArrivalValidationData(transactionHashes, addressHashes);
        signAndSend(data);
    }

    private void signAndSend(MessageArrivalValidationData data){
        messageArrivalValidationCrypto.signMessage(data);
        networkService.sendDataToConnectedDspsByHttp(data);
    }

}
