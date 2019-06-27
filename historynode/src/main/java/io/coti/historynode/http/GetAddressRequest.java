package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetAddressRequest extends Request {
    @NotNull(message = "Address hash must not be null")
    private Hash addressesHash;

    public GetAddressRequest(Hash addressesHash) {
        this.addressesHash = addressesHash;
    }
}