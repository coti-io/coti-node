package io.coti.fullnode.services;

import io.coti.common.communication.interfaces.ISender;
import io.coti.common.data.AddressData;
import io.coti.common.data.Hash;
import io.coti.common.model.Addresses;
import io.coti.common.services.LiveView.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AddressService {
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;

    @Autowired
    private ISender sender;
    @Autowired
    private Addresses addresses;
    @Autowired
    private WebSocketSender webSocketSender;

    public boolean addNewAddress(Hash addressHash) {


        if (!addressExists(addressHash)) {
            AddressData addressData = new AddressData(addressHash);
            addresses.put(addressData);
            log.info("Address {} was successfully inserted", addressHash);
            receivingServerAddresses.forEach(address -> sender.send(addressData, address));
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
        addNewAddress(addressData.getHash());
    }
}