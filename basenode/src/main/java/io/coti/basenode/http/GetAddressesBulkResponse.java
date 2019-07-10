package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.Map;

@Data
public class GetAddressesBulkResponse extends GetEntitiesBulkResponse{

    public Map<Hash, String> entitiesBulkResponses;

    public GetAddressesBulkResponse() {
    }

    public GetAddressesBulkResponse(Map<Hash, String> entitiesBulkResponses) {
        this.entitiesBulkResponses = entitiesBulkResponses;
    }
}
