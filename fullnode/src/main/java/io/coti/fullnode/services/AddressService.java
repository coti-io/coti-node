package io.coti.fullnode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeAddressService;
import io.coti.fullnode.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressService extends BaseNodeAddressService {

    @Autowired
    private WebSocketSender webSocketSender;
    @Autowired
    private MessageArrivalValidationService messageArrivalValidationService;
    @Autowired
    private NetworkService networkService;

    public boolean addAddress(Hash addressHash) {
        AddressData addressData = new AddressData(addressHash);

        if (!super.addNewAddress(addressData)) {
            return false;
        }
        messageArrivalValidationService.addAddressHash(addressData.getHash());

        networkService.sendDataToConnectedDspNodes(addressData);
        continueHandleGeneratedAddress(addressData);
        return true;
    }

    @Override
    protected void continueHandleGeneratedAddress(AddressData addressData) {
        webSocketSender.notifyGeneratedAddress(addressData.getHash());
    }
}
