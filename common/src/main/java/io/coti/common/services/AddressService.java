package io.coti.common.services;

import io.coti.common.data.AddressData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.model.Addresses;
import io.coti.common.model.BaseTransactions;
import io.coti.common.services.interfaces.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressService implements IAddressService {

    @Autowired
    private Addresses addresses;



    @Override
    public boolean addNewAddress(Hash addressHash) {
        if( addresses.getByHash(addressHash) == null){
            addresses.put(new AddressData(addressHash));
            log.info("Address {} was successfully inserted",addressHash);
            return true;
        }
        log.info("Address {} already exists",addressHash);
        return false;
    }

    @Override
    public boolean addressExists(Hash addressHash) {
        if( addresses.getByHash(addressHash) == null){
            return false;
        }
        return true;
    }
}
