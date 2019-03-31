package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeHistoryData;
import org.springframework.stereotype.Service;

@Service
public class DisputeHistory extends Collection<DisputeHistoryData> {

    public DisputeHistory() {
    }

    public void init() {
        super.init();
    }
}
