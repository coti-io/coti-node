package io.coti.common.http;


import java.util.HashMap;


public class AddressesExistsResponse extends BaseResponse {
    public HashMap<String,Boolean> addresses;

    public AddressesExistsResponse() {
        addresses= new HashMap<>();
    }


    public void addAddressToResult(String addressHash, Boolean isExists){
        addresses.putIfAbsent(addressHash,isExists);
    }

}


