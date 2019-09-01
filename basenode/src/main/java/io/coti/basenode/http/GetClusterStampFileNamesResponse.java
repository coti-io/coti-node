package io.coti.basenode.http;

import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetClusterStampFileNamesResponse extends BaseResponse implements ISignValidatable, ISignable, IPropagatable {

    ClusterStampNameData major;
    @NotNull
    private List<ClusterStampNameData> tokenClusterStampNames;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;

//    public GetClusterStampFileNamesResponse() {
//
//    }

    @Override
    public Hash getHash() {
        return signerHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.signerHash = hash;
    }
}
