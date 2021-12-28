package io.coti.trustscore.http;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class NetworkFeeRequest {

    @NotNull
    private @Valid Hash userHash;
    @NotNull
    private @Valid FullNodeFeeData fullNodeFeeData;
    private boolean feeIncluded;

}
