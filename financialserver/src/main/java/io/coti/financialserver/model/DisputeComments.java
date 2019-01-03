package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeCommentData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DisputeComments extends Collection<DisputeCommentData> {

    public DisputeComments() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
