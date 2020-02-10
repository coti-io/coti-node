package io.coti.historynode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.Data;

import java.util.List;

@Data
public class GetAddressBatchResponse implements IResponse {

    private List<AddressData> addresses;

    public GetAddressBatchResponse(List<AddressData> addresses) {
        this.addresses = addresses;
    }

    public GetAddressBatchResponse() {
    }
}
