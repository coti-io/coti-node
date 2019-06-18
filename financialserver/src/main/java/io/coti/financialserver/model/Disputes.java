package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeData;
import org.springframework.stereotype.Service;

@Service
public class Disputes extends Collection<DisputeData> {

    public void init() {
        super.init();
    }
}
