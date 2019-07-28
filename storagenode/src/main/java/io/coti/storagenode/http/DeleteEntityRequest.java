package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class DeleteEntityRequest extends Request {
    @NotEmpty(message = "Hash must not be empty")
    private Hash hash;

    public DeleteEntityRequest(@NotEmpty(message = "Hash must not be empty") Hash hash) {
        this.hash = hash;
    }
}