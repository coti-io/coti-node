package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.PrepareForSnapshotCrypto;
import io.coti.basenode.data.PrepareForSnapshot;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    private static final int MAKE_SNAPSHOT_EACH_TRANSACTION = 1;

    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Autowired
    private PrepareForSnapshotCrypto prepareForSnapshotCrypto;

    @Override
    protected boolean insertNewTransactionIndex(TransactionData transactionData) {
        return true;
    }

    protected void incrementAndGetDspConfirmed() {

        long dspConfirmedLocal = dspConfirmed.incrementAndGet();
        if(dspConfirmedLocal > 11 && dspConfirmedLocal % MAKE_SNAPSHOT_EACH_TRANSACTION == 0) {

            PrepareForSnapshot prepareForSnapshot = new PrepareForSnapshot(dspConfirmedLocal);
            prepareForSnapshotCrypto.signMessage(prepareForSnapshot);

            propagationPublisher.propagate(prepareForSnapshot, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
        }
    }
}
