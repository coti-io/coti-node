package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.ClusterService;
import io.coti.basenode.http.GetSourcesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService extends BaseNodeTransactionService {
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private ClusterService clusterService;

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        dspVoteService.preparePropagatedTransactionForVoting(transactionData);
    }

    public ResponseEntity<IResponse> getSources() {
        List<List<TransactionData>> sourceListsByTrustScore = Collections.unmodifiableList(clusterService.getSourceListsByTrustScore());
        List<TransactionData> sources = sourceListsByTrustScore.stream().flatMap(Collection::stream).collect(Collectors.toList());
        log.info("{}",sources.toString());
        return ResponseEntity.ok(new GetSourcesResponse(sources));
    }
}