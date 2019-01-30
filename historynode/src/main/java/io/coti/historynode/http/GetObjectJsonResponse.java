package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import javafx.util.Pair;

public class GetObjectJsonResponse extends BaseResponse {

    Pair<Hash, String> objectJson;

    public GetObjectJsonResponse(Hash AddressHash, String AddressJson) {
        this.objectJson = new Pair<>(AddressHash, AddressJson);
    }
}
