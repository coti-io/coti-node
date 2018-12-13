package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeItemVoteData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DisputeItemVotes extends Collection<DisputeItemVoteData> {

    public DisputeItemVotes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
