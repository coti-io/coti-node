package io.coti.common.model;

import io.coti.common.data.TransactionVoteData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class TransactionVotes extends Collection<TransactionVoteData> {
    public TransactionVotes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}