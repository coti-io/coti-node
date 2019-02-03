package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotNull;

public class DeleteObjectRequest extends Request {
    @NotNull(message = "Hash must not be blank")
    public Hash hash;

    public DeleteObjectRequest() {

    }
}