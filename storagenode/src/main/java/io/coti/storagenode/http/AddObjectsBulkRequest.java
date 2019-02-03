package io.coti.storagenode.http;


import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class AddObjectsBulkRequest {
    @NotNull(message = "Addresses must not be blank")
    public Map<Hash, String> hashToObjectJsonDataMap;

    public AddObjectsBulkRequest() {

    }
}

