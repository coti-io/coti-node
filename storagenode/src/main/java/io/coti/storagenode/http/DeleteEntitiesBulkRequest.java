package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;

import javax.validation.constraints.NotEmpty;
import java.util.Hashtable;
import java.util.Map;

public class DeleteEntitiesBulkRequest implements IRequest {

    @NotEmpty(message = "hashAndIndexNameMap must not be empty")
    private Map<Hash, String> hashAndIndexNameMap;

    public DeleteEntitiesBulkRequest() {
        hashAndIndexNameMap = new Hashtable<>();
    }

    public DeleteEntitiesBulkRequest(Map<Hash, String> hashAndIndexNameMap) {
        this.hashAndIndexNameMap = hashAndIndexNameMap;
    }
}
