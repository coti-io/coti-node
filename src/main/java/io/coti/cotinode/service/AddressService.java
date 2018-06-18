package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.service.interfaces.IAddressService;
import org.springframework.stereotype.Service;

@Service
public class AddressService implements IAddressService {

    @Override
    public boolean addNewAddress(Hash addressHash) {
        return true;
    }
}
