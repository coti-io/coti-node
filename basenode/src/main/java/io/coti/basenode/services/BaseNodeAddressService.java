package io.coti.basenode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.interfaces.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeAddressService implements IAddressService {
    @Autowired
    private Addresses addresses;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public boolean addNewAddress(Hash addressHash) {
        if (!addressExists(addressHash)) {
            AddressData addressData = new AddressData(addressHash);
            addresses.put(addressData);
            log.info("Address {} was successfully inserted", addressHash);
            return true;
        }
        log.debug("Address {} already exists", addressHash);
        return false;
    }

    public boolean addressExists(Hash addressHash) {
        return addresses.getByHash(addressHash) != null;
    }

    public void handlePropagatedAddress(AddressData addressData) {
        if (!addressExists(addressData.getHash())) {
            addNewAddress(addressData.getHash());
            continueHandleGeneratedAddress(addressData);
        }
    }

    protected void continueHandleGeneratedAddress(AddressData addressData) {

    }
}