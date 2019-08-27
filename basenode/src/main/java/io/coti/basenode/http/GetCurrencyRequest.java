package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class GetCurrencyRequest extends Request implements ISignable, ISignValidatable {

    @NotNull
    public Set<Hash> currenciesHashes;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;
}
