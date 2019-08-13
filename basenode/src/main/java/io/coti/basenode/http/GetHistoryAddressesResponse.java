package io.coti.basenode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class GetHistoryAddressesResponse extends BaseResponse implements ISignValidatable, ISignable {

    @NotEmpty
    private Map<Hash, AddressData> addressHashesToAddresses;
    @NotNull
    private SignatureData signature;
    @NotNull
    private Hash signerHash;

    public GetHistoryAddressesResponse() {
    }

    public GetHistoryAddressesResponse(Map<Hash, AddressData> addressHashesToAddresses) {
        this.addressHashesToAddresses = addressHashesToAddresses;
    }

}