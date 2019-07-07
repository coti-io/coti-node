package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
public class GetEntitiesBulkJsonResponse extends BaseResponse {

    @NotEmpty
    public Map<Hash, String> hashToEntitiesFromDbMap;

    public GetEntitiesBulkJsonResponse(Map<Hash, String> hashToEntitiesFromDbMap) {
        this.hashToEntitiesFromDbMap = hashToEntitiesFromDbMap;
    }
}
