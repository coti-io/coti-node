package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class RepropagateTransactionByAdminRequest {

    @NotNull
    public @Valid Hash transactionHash;
}
