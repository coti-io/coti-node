package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetEntitiesBulkRequest implements IRequest {
    @NotEmpty(message = "Hashes must not be empty")
    private List<Hash> hashes;

    public GetEntitiesBulkRequest() {
        hashes = new ArrayList<>();
    }
}

