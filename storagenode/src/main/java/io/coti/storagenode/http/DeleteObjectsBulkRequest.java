package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class DeleteObjectsBulkRequest extends Request {
    @NotNull(message = "hashAndIndexNameMap must not be blank")
    public Map<Hash, String> hashAndIndexNameMap;

    public DeleteObjectsBulkRequest() {

    }
}
