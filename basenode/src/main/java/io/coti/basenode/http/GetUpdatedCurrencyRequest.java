package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUpdatedCurrencyRequest extends SerializableRequest implements ISignable, ISignValidatable {

    @NotNull
    private Map<CurrencyType, HashSet<Hash>> currencyHashesByType;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;
}
