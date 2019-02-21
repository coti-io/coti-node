package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.Map;

@Data
public class GetObjectBulkJsonResponse extends BaseResponse {

    private Map<Hash, String> hashToObjectsFromDbMap;

    public GetObjectBulkJsonResponse(Map<Hash, String> hashToObjectsFromDbMap) {
        this.hashToObjectsFromDbMap = hashToObjectsFromDbMap;
    }
}
