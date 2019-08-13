package io.coti.basenode.http;


import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Hashtable;
import java.util.Map;

@Data
public class AddEntitiesBulkRequest extends Request {

    @NotEmpty(message = "Entities must not be empty")
    private Map<Hash, String> hashToEntityJsonDataMap;

    public AddEntitiesBulkRequest() {
        hashToEntityJsonDataMap = new Hashtable<>();
    }

    public AddEntitiesBulkRequest(Map<Hash, String> hashToEntityJsonDataMap) {
        this.hashToEntityJsonDataMap = hashToEntityJsonDataMap;
    }

}

