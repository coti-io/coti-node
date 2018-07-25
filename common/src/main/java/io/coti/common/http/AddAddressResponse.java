package io.coti.common.http;

import io.coti.common.data.Hash;
import io.coti.common.http.data.AddressStatus;

public class AddAddressResponse extends BaseResponse {


    public String address;
    public AddressStatus addressStatus;


    public AddAddressResponse(Hash addressHash, AddressStatus status) {
        this(addressHash.toHexString(), status);
    }

    public AddAddressResponse(String address, AddressStatus status) {

        this.address = address;
        this.addressStatus = status;

    }


}
