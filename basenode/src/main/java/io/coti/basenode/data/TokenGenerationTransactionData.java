package io.coti.basenode.data;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
//TODO 9/17/2019 astolia: extend output base transaction?
public class TokenGenerationTransactionData extends BaseTransactionData  {

    private static final long serialVersionUID = -8401239339732339955L;

    public TokenGenerationTransactionData(Hash addressHash, Hash currencyHash, BigDecimal amount, Instant createTime) {
        super(addressHash, currencyHash, amount, createTime);
    }
}
