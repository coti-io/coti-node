package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;

import java.util.Map;

public class GetMultiObjectJsonResponse extends BaseResponse {

    Map<Hash, String> hashToObjectsFromDbMap;

    public GetMultiObjectJsonResponse(Map<Hash, String> hashToObjectsFromDbMap) {
        this.hashToObjectsFromDbMap = hashToObjectsFromDbMap;
    }
}
