package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;

import java.util.Map;

public class GetObjectBulkJsonResponse extends BaseResponse {

    Map<Hash, String> hashToObjectsFromDbMap;

    public GetObjectBulkJsonResponse(Map<Hash, String> hashToObjectsFromDbMap) {
        this.hashToObjectsFromDbMap = hashToObjectsFromDbMap;
    }
}
