package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class RepropagateTransactionRequest implements ISignValidatable {

    @NotNull
    public @Valid Hash transactionHash;
    @NotNull
    public Instant createTime;
    @NotNull
    public @Valid Hash signerHash;
    @NotNull
    public @Valid SignatureData signature;
}

