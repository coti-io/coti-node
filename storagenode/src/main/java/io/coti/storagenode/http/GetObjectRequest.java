package io.coti.storagenode.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotNull;

public class GetObjectRequest extends Request {
    @NotNull(message = "Hash must not be blank")
    public Hash hash;

    public GetObjectRequest() {

    }
}
