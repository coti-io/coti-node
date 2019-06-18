package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetEntitiesBulkRequest extends Request {
    @NotEmpty(message = "Hashes must not be empty")
    private List<Hash> hashes;

    public GetEntitiesBulkRequest() {
        hashes = new ArrayList<>();
    }
}

