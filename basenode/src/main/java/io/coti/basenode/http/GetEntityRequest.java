package io.coti.basenode.http;


import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class GetEntityRequest extends Request {
    @NotEmpty(message = "Hash must not be empty")
    private Hash hash;

    public GetEntityRequest(@NotEmpty(message = "Hash must not be empty") Hash hash) {
        this.hash = hash;
    }
}
