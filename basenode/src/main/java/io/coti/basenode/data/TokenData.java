package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Data
public class TokenData {


    private BigDecimal amount;
    private int scale;
    private BigDecimal totalSupply;

    public TokenData() {
        this.amount = BigDecimal.ZERO;
        this.scale = 0;
        this.totalSupply = BigDecimal.ZERO;
    }

    public TokenData(BigDecimal amount, int scale, BigDecimal totalSupply) {
        this.amount = amount;
        this.scale = scale;
        this.totalSupply = totalSupply;
    }
}
