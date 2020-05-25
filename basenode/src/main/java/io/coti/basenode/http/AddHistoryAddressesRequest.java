package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
public class AddHistoryAddressesRequest implements ISignable, ISignValidatable, IRequest {

    @NotEmpty(message = "Entities must not be empty")
    private Map<Hash, String> hashToAddressDataJsonMap;
    private Hash signerHash;
    private SignatureData signature;

    private AddHistoryAddressesRequest() {
    }

    public AddHistoryAddressesRequest(Map<Hash, String> hashToAddressDataJsonMap) {
        this.hashToAddressDataJsonMap = hashToAddressDataJsonMap;
    }

}

