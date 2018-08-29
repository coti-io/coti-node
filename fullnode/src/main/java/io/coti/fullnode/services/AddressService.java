package io.coti.fullnode.services;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.communication.interfaces.ISender;
import io.coti.common.data.AddressData;
import io.coti.common.data.Hash;
import io.coti.common.services.BaseNodeAddressService;
import io.coti.common.services.LiveView.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    @Autowired
    private WebSocketSender webSocketSender;

    @Autowired
    private ISender sender;

    public boolean addAddress(Hash addressHash) {
        if (!super.addNewAddress(addressHash)) {
            return false;
        }
        receivingServerAddresses.forEach(address -> sender.send(new AddressData(addressHash), address));
        return true;
    }

    @Override
    protected void continueHandlePropagatedAddress(AddressData addressData) {
        webSocketSender.notifyGeneratedAddress(addressData.getHash());
    }
}
