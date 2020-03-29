package io.coti.basenode.http;

import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

@Data
public class GetClusterStampFileNamesResponse extends BaseResponse implements ISignValidatable, ISignable, IPropagatable {

    @NotEmpty
    private @Valid ClusterStampNameData currencies;
    @NotEmpty
    private @Valid ClusterStampNameData major;
    @NotEmpty
    private String clusterStampBucketName;
    @NotEmpty
    private @Valid Hash signerHash;
    @NotEmpty
    private @Valid SignatureData signature;

    @Override
    public Hash getHash() {
        return signerHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.signerHash = hash;
    }
}
