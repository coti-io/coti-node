package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Data
public class ClusterStampCurrencyData {

    private BigDecimal totalSupply;
    private BigDecimal amount;
    private int scale;

    public ClusterStampCurrencyData(CurrencyData currencyData) {
        this.totalSupply = currencyData.getTotalSupply();
        this.amount = currencyData.getTotalSupply();
        this.scale = currencyData.getScale();
    }
}
