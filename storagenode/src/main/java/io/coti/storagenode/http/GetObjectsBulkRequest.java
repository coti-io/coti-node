package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotNull;
import java.util.List;

public class GetObjectsBulkRequest extends Request {
    @NotNull(message = "Hashes must not be blank")
    public List<Hash> hashes;

    public GetObjectsBulkRequest() {

    }
}

