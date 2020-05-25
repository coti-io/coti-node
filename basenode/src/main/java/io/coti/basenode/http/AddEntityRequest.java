package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AddEntityRequest implements IRequest {

    @NotEmpty(message = "Hash must not be empty")
    private Hash hash;
    @NotEmpty(message = "Entity must not be empty")
    private String entityJson;

    public AddEntityRequest(Hash hash, String entityJson) {
        this.hash = hash;
        this.entityJson = entityJson;
    }
}
