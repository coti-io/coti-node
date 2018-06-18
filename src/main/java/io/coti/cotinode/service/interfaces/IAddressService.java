package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;

public interface IAddressService {

    boolean addNewAddress(Hash addressHash);
}
