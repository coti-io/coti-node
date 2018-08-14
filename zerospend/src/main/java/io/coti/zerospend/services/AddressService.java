package io.coti.zerospend.services;

import io.coti.common.data.AddressData;
import io.coti.common.model.Addresses;
import io.coti.zerospend.services.interfaces.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AddressService implements IAddressService {

    @Autowired
    private Addresses addresses;

    public void handlePropagatedAddress(AddressData addressData) {
        if(!addressExists(addressData)){
            addNewAddress(addressData);
        }
    }

    private void addNewAddress(AddressData addressData) {
        addresses.put(addressData);
        log.info("Address {} was successfully inserted", addressData.getHash());
    }

    private boolean addressExists(AddressData addressData) {
        return addresses.getByHash(addressData.getHash()) != null;
    }

}
