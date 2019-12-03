package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class RepropagateTransactionRequest extends Request implements ISignValidatable {
    @NotNull
    public Hash transactionHash;
    @NotNull
    public Instant createTime;
    @NotNull
    public Hash signerHash;
    @NotNull
    public SignatureData signature;
}

