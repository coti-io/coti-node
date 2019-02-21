package io.coti.dspnode.model;

import io.coti.basenode.model.Collection;
import io.coti.dspnode.data.NotTotalConfirmedTransactionHash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class NotTotalConfirmedTransactionHashes extends Collection<NotTotalConfirmedTransactionHash> {

    public NotTotalConfirmedTransactionHashes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
