package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Data
public class ClusterStampCurrencyData {

    private final BigDecimal totalSupply;
    private BigDecimal amount;
    private final int scale;
    private final boolean nativeCurrency;

    public ClusterStampCurrencyData(CurrencyData currencyData) {
        this.totalSupply = currencyData.getTotalSupply();
        this.amount = currencyData.getTotalSupply();
        this.scale = currencyData.getScale();
        this.nativeCurrency = currencyData.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN);
    }
}
