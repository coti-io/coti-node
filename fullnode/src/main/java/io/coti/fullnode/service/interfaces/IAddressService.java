package io.coti.fullnode.service.interfaces;

import io.coti.common.data.Hash;

public interface IAddressService {

    boolean addNewAddress(Hash addressHash);
}
