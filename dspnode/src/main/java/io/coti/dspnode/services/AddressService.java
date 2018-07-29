package io.coti.dspnode.services;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.data.AddressData;
import io.coti.common.model.Addresses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AddressService {
    @Autowired
    private Addresses addresses;
    @Autowired
    private IPropagationPublisher propagationPublisher;

    public void addNewAddress(AddressData addressData) {
        addresses.put(addressData);
        log.info("Address {} was successfully inserted", addressData.getHash());
    }

    public boolean addressExists(AddressData addressData) {
        return addresses.getByHash(addressData.getHash()) != null;
    }

    public String handleReceivedAddress(AddressData addressData) {
        if (!addressExists(addressData)) {
            addNewAddress(addressData);
            propagationPublisher.propagateAddress(addressData, AddressData.class.getName() + "Full Nodes");
            propagationPublisher.propagateAddress(addressData, AddressData.class.getName() + "DSP Nodes");
            log.info("Address {} was successfully added", addressData);
        } else {
            log.info("Address {} already exists", addressData);
        }
        return "OK";
    }

    public void handlePropagatedAddress(AddressData addressData) {
        if(!addressExists(addressData)){
            addNewAddress(addressData);
            propagationPublisher.propagateAddress(addressData, AddressData.class.getName() + "Full Nodes");
        }
    }
}
