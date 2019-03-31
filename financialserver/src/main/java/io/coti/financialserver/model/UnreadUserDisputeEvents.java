package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.UnreadUserDisputeEventData;
import org.springframework.stereotype.Service;

@Service
public class UnreadUserDisputeEvents extends Collection<UnreadUserDisputeEventData> {

    public UnreadUserDisputeEvents() {
    }

    public void init() {
        super.init();
    }
}
