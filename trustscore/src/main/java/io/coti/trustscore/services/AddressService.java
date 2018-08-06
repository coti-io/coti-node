package io.coti.trustscore.services;

import io.coti.common.communication.interfaces.ISender;
import io.coti.common.data.AddressData;
import io.coti.common.data.Hash;
import io.coti.common.model.Addresses;
import io.coti.common.services.LiveView.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressService {

    @Autowired
    private Addresses addresses;
    @Autowired
    private WebSocketSender webSocketSender;

    public boolean addNewAddress(Hash addressHash) {
        if (!addressExists(addressHash)) {
            AddressData addressData = new AddressData(addressHash);
            addresses.put(addressData);
            log.info("Address {} was successfully inserted", addressHash);
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