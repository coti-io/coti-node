package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AddEntityRequest extends Request {
    @NotEmpty(message = "Hash must not be empty")
    private Hash hash;

    @NotEmpty(message = "Address object must not be empty")
    private String entityJson;

    public AddEntityRequest(@NotEmpty(message = "Hash must not be empty") Hash hash, @NotEmpty(message = "Address object must not be empty") String entityJson) {
        this.hash = hash;
        this.entityJson = entityJson;
    }
}
