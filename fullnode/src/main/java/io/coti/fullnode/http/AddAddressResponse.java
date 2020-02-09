package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.data.AddressStatus;
import lombok.Data;

@Data
public class AddAddressResponse extends BaseResponse {

    private String address;
    private AddressStatus addressStatus;

    public AddAddressResponse(String address, AddressStatus status) {
        this.address = address;
        this.addressStatus = status;
    }
}
