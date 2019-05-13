package io.coti.dspnode.services;

import io.coti.basenode.data.AddressDataHash;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionDataHash;
import io.coti.basenode.model.AddressDataHashes;
import io.coti.basenode.model.TransactionDataHashes;
import io.coti.basenode.services.interfaces.BaseNodeMessageArrivalValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageArrivalValidationService extends BaseNodeMessageArrivalValidationService {

    //TODO 5/13/2019 astolia: make general add hash by data name using enums. move to other realer and router.

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

}