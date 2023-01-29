package io.coti.dspnode.services;

import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.dspnode.model.UnconfirmedTransactionDspVotes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Primary
@Service
public class NodeServiceManager extends BaseNodeServiceManager {

    public static UnconfirmedTransactionDspVotes unconfirmedTransactionDspVotes;

    @Autowired
    public UnconfirmedTransactionDspVotes autowiredUnconfirmedTransactionDspVotes;

}
