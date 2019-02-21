package io.coti.trustscore.http;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkFeeData;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NetworkFeeValidateRequest {
    @NotNull
    private FullNodeFeeData fullNodeFeeData;
    @NotNull
    private NetworkFeeData networkFeeData;
    @NotNull
    private Hash userHash;

}
