package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
public class EntitiesBulkJsonResponse extends Response{

    @NotEmpty
    public Map<Hash, String> hashToEntitiesFromDbMap;

    public EntitiesBulkJsonResponse(Map<Hash, String> hashToEntitiesFromDbMap) {
        this.hashToEntitiesFromDbMap = hashToEntitiesFromDbMap;
    }

    public EntitiesBulkJsonResponse(Map<Hash, String> hashToEntitiesFromDbMap, String message, String status) {
        super(message, status);
        this.hashToEntitiesFromDbMap = hashToEntitiesFromDbMap;
    }

    public EntitiesBulkJsonResponse(){

    }
}