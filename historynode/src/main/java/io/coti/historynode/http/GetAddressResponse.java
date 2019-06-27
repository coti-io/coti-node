package io.coti.historynode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.Data;

@Data
public class GetAddressResponse implements IResponse {
    AddressData address;

    public GetAddressResponse(AddressData address) {
        this.address = address;
    }

    public GetAddressResponse() {
    }
}
