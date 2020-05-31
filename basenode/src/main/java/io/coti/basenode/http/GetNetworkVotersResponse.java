package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
public class GetNetworkVotersResponse extends BaseResponse implements ISignValidatable, ISignable, IPropagatable {

    @NotEmpty
    private List<Hash> allCurrentValidators;
    @NotNull
    private Instant createTime = Instant.now();
    @NotEmpty
    private @Valid Hash signerHash;
    @NotEmpty
    private @Valid SignatureData signature;

    public GetNetworkVotersResponse(List<Hash> allCurrentValidators) {
        this.allCurrentValidators = allCurrentValidators;
    }

    @Override
    public Hash getHash() {
        return signerHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.signerHash = hash;
    }

}
