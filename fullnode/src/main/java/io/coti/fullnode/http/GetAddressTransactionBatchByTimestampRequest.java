package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.fullnode.http.data.TimeOrder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.util.Set;

@Data
public class GetAddressTransactionBatchByTimestampRequest {

    @NotNull(message = "Address Hashes must not be blank")
    private Set<@Valid Hash> addresses;
    private @Valid Instant startTime;
    private @Valid Instant endTime;
    private @Valid @Positive Integer limit;
    private @Valid TimeOrder order;
    private boolean includeRuntimeTrustScore;

    private GetAddressTransactionBatchByTimestampRequest() {
    }

    public GetAddressTransactionBatchByTimestampRequest(Set<Hash> addresses, Instant startTime, Instant endTime, Integer limit, TimeOrder order, boolean isIncludeRuntimeTrustScore) {
        this.addresses = addresses;
        this.startTime = startTime;
        this.endTime = endTime;
        this.limit = limit;
        this.order = order;
        this.includeRuntimeTrustScore = isIncludeRuntimeTrustScore;
    }
}
