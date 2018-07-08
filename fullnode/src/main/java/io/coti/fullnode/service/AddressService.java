package io.coti.fullnode.service;

import io.coti.common.data.AddressData;
import io.coti.common.data.Hash;
import io.coti.common.model.Addresses;
import io.coti.fullnode.service.interfaces.IAddressService;
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
}
