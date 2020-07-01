package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SetNewClusterStampsRequest extends Request implements ISignValidatable, ISignable {

    @NotEmpty
    private String folderPath;
    @NotEmpty
    private String clusterStampFileName;
    @NotNull
    private @Valid Hash expectedClusterStampHash;

    @NotNull
    private @Valid Hash signerHash;
    @NotNull
    private @Valid SignatureData signature;

    public SetNewClusterStampsRequest() {
    }

    public SetNewClusterStampsRequest(String folderPath, String clusterStampFileName, Hash expectedClusterStampHash) {
        this.folderPath = folderPath;
        this.clusterStampFileName = clusterStampFileName;
        this.expectedClusterStampHash = expectedClusterStampHash;
    }
}
