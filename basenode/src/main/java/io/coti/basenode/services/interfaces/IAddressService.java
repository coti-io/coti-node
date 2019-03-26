package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;

public interface IAddressService {
    void init();

    boolean addNewAddress(AddressData addressData);

    boolean addressExists(Hash addressHash);

    void handlePropagatedAddress(AddressData addressData);

    boolean validateAddress(Hash addressHash);


}
