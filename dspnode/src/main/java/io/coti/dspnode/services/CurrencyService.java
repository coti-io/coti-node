package io.coti.dspnode.services;


import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.InitiatedTokenNoticeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeCurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Override
    public void handleInitiatedTokenNotice(InitiatedTokenNoticeData initiatedTokenNoticeData) {
        super.handleInitiatedTokenNotice(initiatedTokenNoticeData);
        propagationPublisher.propagate(initiatedTokenNoticeData, Arrays.asList(NodeType.FullNode));
    }
}
