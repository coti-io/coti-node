package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.TokenSaleDistributionData;
import org.springframework.stereotype.Service;

@Service
public class TokenSaleDistributions extends Collection<TokenSaleDistributionData> {

    public TokenSaleDistributions() {
    }

    public void init() {
        super.init();
    }

}
