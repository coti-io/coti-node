package io.coti.zerospend.services.interfaces;

import io.coti.common.data.AddressData;

public interface IAddressService {
    void handlePropagatedAddress(AddressData addressData);
}
