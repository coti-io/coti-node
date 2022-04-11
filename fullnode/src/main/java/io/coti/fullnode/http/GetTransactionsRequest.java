package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class GetTransactionsRequest {

    @NotEmpty
    private List<@Valid Hash> transactionHashes;
    private boolean extended;
    private boolean includeRuntimeTrustScore;

}
