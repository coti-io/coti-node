package io.coti.historynode.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class GetTransactionsRequest extends Request implements ISignValidatable {


    private Hash transactionAddress;
    private Instant startDate;
    private Instant endDate;

    @NotNull
    public @Valid Hash userHash;
    @NotNull
    public @Valid SignatureData userSignature;

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }
}

