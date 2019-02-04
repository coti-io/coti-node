package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.SnapshotPreparationCrypto;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.SnapshotPreparationData;
import io.coti.basenode.services.BaseNodeIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class IndexService extends BaseNodeIndexService {

    private static final int MAKE_SNAPSHOT_EACH_TRANSACTION = 1;

    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Autowired
    private SnapshotPreparationCrypto snapshotPreparationCrypto;

    public void incrementAndGetDspConfirmed(long dspConfirmed) {

        if(dspConfirmed > 11 && dspConfirmed % MAKE_SNAPSHOT_EACH_TRANSACTION == 0) {

            SnapshotPreparationData snapshotPreparationData = new SnapshotPreparationData(dspConfirmed);
            snapshotPreparationCrypto.signMessage(snapshotPreparationData);

            propagationPublisher.propagate(snapshotPreparationData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
        }
    }
}
