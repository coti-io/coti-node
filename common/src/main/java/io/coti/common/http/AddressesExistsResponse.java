package io.coti.common.http;


import io.coti.common.http.data.GetAddressData;

import java.util.List;


public class AddressesExistsResponse extends BaseResponse {
    public List<GetAddressData> addresses;

    public AddressesExistsResponse(List<GetAddressData> addressExists) {
        this.addresses = addressExists;
    }
}


