package io.coti.basenode.model;

import io.coti.basenode.data.NotTotalConfirmedTransactionHash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class NotTotalConfirmedTransactionHashes extends Collection<NotTotalConfirmedTransactionHash> {

    public NotTotalConfirmedTransactionHashes() {
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
    }
}
