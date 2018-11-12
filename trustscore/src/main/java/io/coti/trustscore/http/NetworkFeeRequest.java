package io.coti.trustscore.http;

import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NetworkFeeRequest {

    @NotNull
    Hash userHash;

    @NotNull
    FullNodeFeeData fullNodeFeeData;


}
