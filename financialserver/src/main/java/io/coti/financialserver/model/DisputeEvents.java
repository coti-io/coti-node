package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeEventData;
import org.springframework.stereotype.Service;

@Service
public class DisputeEvents extends Collection<DisputeEventData> {

    public void init() {
        super.init();
    }
}
