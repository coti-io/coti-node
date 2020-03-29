package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

@Data
public class CurrencyTypeRegistrationData extends CurrencyTypeData implements ISignable, ISignValidatable {

    private Hash currencyHash;

    public CurrencyTypeRegistrationData(Hash currencyHash, CurrencyTypeData currencyTypeData) {
        super(currencyTypeData);
        this.currencyHash = currencyHash;
    }
}
