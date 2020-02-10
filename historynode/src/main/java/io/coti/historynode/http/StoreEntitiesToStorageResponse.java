package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import lombok.Data;

import java.util.HashMap;

@Data
public class StoreEntitiesToStorageResponse extends Response {

    private HashMap<Hash, Boolean> entitiesSentToStorage;

    public StoreEntitiesToStorageResponse(HashMap<Hash, Boolean> entitiesSentToStorage) {
        this.entitiesSentToStorage = entitiesSentToStorage;
    }

    public StoreEntitiesToStorageResponse() {
        this.entitiesSentToStorage = new HashMap<>();
    }
}
