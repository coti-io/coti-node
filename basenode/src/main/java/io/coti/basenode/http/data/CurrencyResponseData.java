package io.coti.basenode.http.data;

import io.coti.basenode.data.CurrencyData;
import lombok.Data;

@Data
public class CurrencyResponseData {

    CurrencyData currencyData;

    public CurrencyResponseData() {
    }

    public CurrencyResponseData(CurrencyData currencyData) {
        this.currencyData = currencyData;
    }
}
