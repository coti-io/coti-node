package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetAddressesRequest extends Request {
    @NotEmpty(message = "Addresses hashes must not be empty")
    private List<Hash> addressesHashes;

    public GetAddressesRequest() {
        addressesHashes = new ArrayList<>();
    }
}