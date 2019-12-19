package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AddressesExistsResponse extends BaseResponse {

    private Map<String, Boolean> addresses;

    public AddressesExistsResponse() {
        addresses = new LinkedHashMap<>();
    }

    public AddressesExistsResponse(Map<String, Boolean> addresses) {
        this.addresses = addresses;
    }

    public void addAddressToResult(String addressHash, Boolean isExists) {
        addresses.putIfAbsent(addressHash, isExists);
    }

}


