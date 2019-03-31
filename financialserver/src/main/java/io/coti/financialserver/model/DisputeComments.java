package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeCommentData;
import org.springframework.stereotype.Service;

@Service
public class DisputeComments extends Collection<DisputeCommentData> {

    public DisputeComments() {
    }

    public void init() {
        super.init();
    }
}
