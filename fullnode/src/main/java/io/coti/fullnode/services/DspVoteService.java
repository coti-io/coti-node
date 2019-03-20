package io.coti.fullnode.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.model.TransactionDataHashes;
import io.coti.basenode.services.BaseNodeDspVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DspVoteService extends BaseNodeDspVoteService {

    @Autowired
    private TransactionDataHashes transactionDataHashes;

    @Override
    public void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
        if (transactionDataHashes.exists(dspConsensusResult.getHash())){
            log.debug("Removed hash from transactionData as it was received in DspConsensusResult");
            transactionDataHashes.deleteByHash(dspConsensusResult.getHash());
        }
    }

}
