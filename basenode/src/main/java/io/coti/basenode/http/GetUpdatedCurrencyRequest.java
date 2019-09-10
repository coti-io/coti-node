package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Map;

@Data
public class GetUpdatedCurrencyRequest extends SerializableRequest implements ISignable, ISignValidatable {

    @NotNull
    public Map<CurrencyType, HashSet<Hash>> currencyHashesByType;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;
}
