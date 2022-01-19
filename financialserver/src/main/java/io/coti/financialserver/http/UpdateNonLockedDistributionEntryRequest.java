package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class UpdateNonLockedDistributionEntryRequest {

    @NotNull
    private @Valid Hash hash;
    @NotNull
    private @Valid Hash hashByDate;
}
