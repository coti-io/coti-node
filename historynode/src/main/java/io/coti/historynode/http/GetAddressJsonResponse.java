package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import javafx.util.Pair;

public class GetAddressJsonResponse extends BaseResponse {

    Pair<Hash, String> Address;

    public GetAddressJsonResponse(Hash AddressHash, String AddressJson) {
        this.Address = new Pair<>(AddressHash, AddressJson);
    }
}
