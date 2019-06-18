package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DailyFundDistributionFileData;
import org.springframework.stereotype.Service;

@Service
public class DailyFundDistributionFiles extends Collection<DailyFundDistributionFileData> {

    @Override
    public void init() {
        super.init();
    }

}
