package io.coti.historynode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetAddressResponse extends Response {
    @NotNull(message = "Address hash must not be null")
    private Hash addressesHash;

    private AddressData address;

    public GetAddressResponse(Hash addressesHash, AddressData address) {
        this.addressesHash = addressesHash;
        this.address = address;
    }

    public GetAddressResponse() {
    }
}
