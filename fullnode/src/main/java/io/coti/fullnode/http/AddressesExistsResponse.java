package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class AddressesExistsResponse extends BaseResponse {

    public LinkedHashMap<String, Boolean> addresses;

    public AddressesExistsResponse() {

    }

    public AddressesExistsResponse(LinkedHashMap<String, Boolean> addresses) {
        this.addresses = addresses;
    }

}


