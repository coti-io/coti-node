package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetEntitiesBulkRequest extends Request {
//    @NotEmpty(message = "Hashes must not be empty")
    private List<Hash> hashes;

    public GetEntitiesBulkRequest() {
    }

    // TODO: Temporary change for checking serialization issues
public GetEntitiesBulkRequest(List<Hash> hashes) {
        this.hashes = hashes;
    }

}

