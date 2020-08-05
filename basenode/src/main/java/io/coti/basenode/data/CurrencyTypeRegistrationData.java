package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CurrencyTypeRegistrationData extends CurrencyTypeData implements ISignable, ISignValidatable {

    private String symbol;

    public CurrencyTypeRegistrationData(String symbol, CurrencyTypeData currencyTypeData) {
        super(currencyTypeData);
        this.symbol = symbol;
    }
}
