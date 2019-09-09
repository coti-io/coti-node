package io.coti.trustscore.http;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NetworkFeeRequest {  // todo why not extends request ?

    @NotNull
    private Hash userHash;
    @NotNull
    private FullNodeFeeData fullNodeFeeData;
    private boolean feeIncluded;

}
