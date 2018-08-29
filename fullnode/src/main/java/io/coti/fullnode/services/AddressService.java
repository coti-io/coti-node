package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeAddressService;
import io.coti.basenode.services.LiveView.WebSocketSender;
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
