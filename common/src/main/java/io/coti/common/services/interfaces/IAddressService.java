package io.coti.common.services.interfaces;

import io.coti.common.data.Hash;

public interface IAddressService {

    boolean addNewAddress(Hash addressHash);

    boolean addressExists(Hash addressHash);
}
