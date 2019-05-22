package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DailyFundDistributionData;
import org.springframework.stereotype.Service;

@Service
public class DailyFundDistribution extends Collection<DailyFundDistributionData> {

    public DailyFundDistribution() {}

    @Override
    public void init() {
        super.init();
    }
}
