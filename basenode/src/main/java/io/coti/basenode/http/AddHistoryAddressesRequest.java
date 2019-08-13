package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
public class AddHistoryAddressesRequest extends Request implements ISignable, ISignValidatable {

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

