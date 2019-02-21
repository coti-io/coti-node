package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Hashtable;
import java.util.Map;

@Data
public class AddEntitiesBulkRequest extends Request {
    @NotEmpty(message = "Addresses must not be empty")
    private Map<Hash, String> hashToObjectJsonDataMap;

    public AddEntitiesBulkRequest() {
        hashToObjectJsonDataMap = new Hashtable<>();
    }
}