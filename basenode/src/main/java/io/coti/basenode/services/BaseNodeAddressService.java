package io.coti.basenode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.model.Addresses;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeAddressService implements IAddressService {
    @Autowired
    private Addresses addresses;
    @Autowired
    private IValidationService validationService;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public boolean addNewAddress(AddressData addressData) {
        if (!addressExists(addressData.getHash())) {
            addresses.put(addressData);
            log.info("Address {} was successfully inserted", addressData.getHash());
            return true;
        }
        log.debug("Address {} already exists", addressData.getHash());
        return false;
    }

    @Override
    public boolean addressExists(Hash addressHash) {
        return addresses.getByHash(addressHash) != null;
    }

    public void handlePropagatedAddress(AddressData addressData) {
        try {
            if (addressExists(addressData.getHash())) {
                log.debug("Address {} already exists", addressData.getHash());
                return;
            }
            if (!validateAddress(addressData.getHash())) {
                log.error("Invalid address {}", addressData.getHash());
                return;
            }
            addNewAddress(addressData);
            continueHandleGeneratedAddress(addressData);
        } catch (Exception e) {
            log.error("Error at handlePropagatedAddress");
            e.printStackTrace();
        }
    }

    protected void continueHandleGeneratedAddress(AddressData addressData) {

    }

    @Override
    public boolean validateAddress(Hash addressHash) {
        return validationService.validateAddress(addressHash);
    }
}