package io.coti.basenode.model;

import io.coti.basenode.data.TransactionVoteData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionVotes extends Collection<TransactionVoteData> {

    public void init() {
        super.init();
    }
}