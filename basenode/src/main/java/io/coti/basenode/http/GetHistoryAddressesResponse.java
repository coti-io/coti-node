package io.coti.basenode.http;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetHistoryAddressesResponse extends BaseResponse implements ISignValidatable, ISignable {

    @NotEmpty
    private Map<Hash, AddressData> addressHashesToAddresses;
    @NotNull
    private Instant createTime;
    @NotNull
    private SignatureData signature;
    @NotNull
    private Hash signerHash;

    public GetHistoryAddressesResponse() {
    }

    public GetHistoryAddressesResponse(Map<Hash, AddressData> addressHashesToAddresses) {
        this.addressHashesToAddresses = addressHashesToAddresses;
        this.createTime = Instant.now();
    }

}