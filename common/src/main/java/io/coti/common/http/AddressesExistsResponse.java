package io.coti.common.http;

import java.util.LinkedHashMap;


public class AddressesExistsResponse extends BaseResponse {
    public LinkedHashMap<String, Boolean> addresses;

    public AddressesExistsResponse() {
        addresses = new LinkedHashMap<>();
    }


    public void addAddressToResult(String addressHash, Boolean isExists) {
        addresses.putIfAbsent(addressHash, isExists);
    }

}


