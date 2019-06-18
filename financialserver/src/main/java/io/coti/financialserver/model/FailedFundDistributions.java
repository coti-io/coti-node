package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.FailedFundDistributionData;
import org.springframework.stereotype.Service;

@Service
public class FailedFundDistributions extends Collection<FailedFundDistributionData> {

    @Override
    public void init() {
        super.init();
    }

}
