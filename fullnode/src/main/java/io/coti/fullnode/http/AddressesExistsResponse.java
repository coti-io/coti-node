package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class AddressesExistsResponse extends BaseResponse {

    private LinkedHashMap<String, Boolean> addresses;

    public AddressesExistsResponse() {
        addresses = new LinkedHashMap<>();
    }

    public AddressesExistsResponse(LinkedHashMap<String, Boolean> addresses) {
        this.addresses = addresses;
    }

    public void addAddressToResult(String addressHash, Boolean isExists) {
        addresses.putIfAbsent(addressHash, isExists);
    }

}


