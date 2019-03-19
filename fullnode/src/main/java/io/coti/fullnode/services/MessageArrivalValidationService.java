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

    @Override
    @Scheduled(fixedDelay = 5000L, initialDelay = 1000L)
    public void checkDspReceivedMessages(){

        log.info("Scheduled task checkDspReceivedMessages");
        Set<TransactionDataHash> transactionHashes = transactionDataHashes.getHashes();
        Set<AddressDataHash> addressHashes = addressDataHashes.getHashes();

        MessageArrivalValidationData data = new MessageArrivalValidationData(/*new Hash("TODO"), */transactionHashes, addressHashes);
        signAndSend(data);

        //TODO 3/19/2019 astolia: Decide about the argument for Hash
        if(transactionHashes.isEmpty() && addressHashes.isEmpty()){
            log.info("Both Empty");
//            MessageArrivalValidationData data = new MessageArrivalValidationData(/*new Hash("TODO"), */transactionHashes, addressHashes);
//            signAndSend(data);
        }

        //TODO 3/19/2019 astolia: Not sure if need this.
        else if(transactionHashes.isEmpty()){
            log.info("transactionHashes is Empty");
            //TODO 3/19/2019 astolia: handle data
//            MessageArrivalValidationData data = new MessageArrivalValidationData(/*new Hash("TODO"), */transactionHashes, addressHashes);
//            signAndSend(null);
        }
        //addressHashes is empty
        else{
            log.info("addressHashes is Empty");
            //TODO 3/19/2019 astolia: handle data
//            signAndSend(null);
        }
    }

    private void signAndSend(MessageArrivalValidationData data){
        messageArrivalValidationCrypto.signMessage(data);
        networkService.sendDataToConnectedDspsByHttp(data);
    }

}
