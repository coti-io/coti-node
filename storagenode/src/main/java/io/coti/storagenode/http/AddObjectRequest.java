package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddObjectRequest extends Request {
    @NotNull(message = "Hash must not be blank")
    private Hash hash;

    @NotNull(message = "Address object must not be blank")
    private String objectJson;
}
