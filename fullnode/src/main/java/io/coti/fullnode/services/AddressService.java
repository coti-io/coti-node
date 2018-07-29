package io.coti.fullnode.services;

import io.coti.common.communication.interfaces.ISender;
import io.coti.common.communication.interfaces.publisher.IPropagationPublisher;
import io.coti.common.data.AddressData;
import io.coti.common.data.Hash;
import io.coti.common.model.Addresses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressService {

    @Autowired
    private ISender sender;
    @Autowired
    private Addresses addresses;

    public boolean addNewAddress(Hash addressHash) {
        if (!addressExists(addressHash)) {
            AddressData addressData = new AddressData(addressHash);
            addresses.put(addressData);
            log.info("Address {} was successfully inserted", addressHash);
            sender.sendAddress(addressData);
            return true;
        }
        log.info("Address {} already exists", addressHash);
        return false;
    }

    public boolean addressExists(Hash addressHash) {
        return addresses.getByHash(addressHash) != null;
    }

    public void handlePropagatedAddress(AddressData addressData) {
        addNewAddress(addressData.getHash());
    }
}