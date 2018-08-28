package io.coti.common.services;

import io.coti.common.data.NodeType;
import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.data.AddressData;
import io.coti.common.data.Hash;
import io.coti.common.model.Addresses;
import io.coti.common.services.LiveView.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class BaseNodeAddressService {
    @Autowired
    private Addresses addresses;
    @Autowired
    private WebSocketSender webSocketSender;
    @Autowired
    private IPropagationPublisher propagationPublisher;

    public void init() {
    }

    public boolean addNewAddress(Hash addressHash) {
        if (!addressExists(addressHash)) {
            AddressData addressData = new AddressData(addressHash);
            addresses.put(addressData);
            log.info("Address {} was successfully inserted", addressHash);
            webSocketSender.notifyGeneratedAddress(addressHash);
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
            continueHandlePropagatedAddress(addressData);
        }
    }

    protected void continueHandlePropagatedAddress(AddressData addressData) {

    }
}