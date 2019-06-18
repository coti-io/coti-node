package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DailyFundDistributionData;
import org.springframework.stereotype.Service;

@Service
public class DailyFundDistributions extends Collection<DailyFundDistributionData> {

    @Override
    public void init() {
        super.init();
    }
}
