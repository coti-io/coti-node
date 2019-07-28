package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotEmpty;
import java.util.Hashtable;
import java.util.Map;

public class DeleteEntitiesBulkRequest extends Request {
    @NotEmpty(message = "hashAndIndexNameMap must not be empty")
    private Map<Hash, String> hashAndIndexNameMap;

    public DeleteEntitiesBulkRequest() {
        hashAndIndexNameMap = new Hashtable<>();
    }

    public DeleteEntitiesBulkRequest(@NotEmpty(message = "hashAndIndexNameMap must not be empty") Map<Hash, String> hashAndIndexNameMap) {
        this.hashAndIndexNameMap = hashAndIndexNameMap;
    }
}
