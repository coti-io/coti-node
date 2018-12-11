package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeCommentData;

@Service
public class DisputeComments extends Collection<DisputeCommentData> {

    public DisputeComments() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
