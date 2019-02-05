package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.DspNodeReadyForClusterStampData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.model.ClusterStamp;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private Hash clusterStampCurrentHash;
    private Hash clusterStampInProgressHash;
    private int dspNodesMajority;

    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ClusterStamp clusterStamp;
    @Autowired
    private DspVoteService dspVoteService;

    @PostConstruct
    private void init() {
        clusterStampCurrentHash = new Hash("current");
        clusterStampInProgressHash = new Hash("inProgress");
        dspNodesMajority = 1;
    }

    @Override
    public void dspNodeReadyForClusterStamp(DspNodeReadyForClusterStampData dspNodeReadyForClusterStampData) {

        log.debug("Ready for cluster stamp propagated message received from DSP to ZS");

        ClusterStampData clusterStampData = clusterStamp.getByHash(clusterStampInProgressHash);

        if ( clusterStampData == null ) {
            clusterStampData = new ClusterStampData(clusterStampInProgressHash);
        }

        clusterStampData.getDspNodeReadyForClusterStampDataList().add(dspNodeReadyForClusterStampData);

        if ( clusterStampData.getDspNodeReadyForClusterStampDataList().size() >= dspNodesMajority ) {
            log.info("Stop dsp vote service from sum and save dsp votes");
            dspVoteService.stopSumAndSaveVotes();
        }

        clusterStamp.put(clusterStampData);
    }

    public void makeAndPropagateClusterStamp() {

        ClusterStampData clusterStampData = clusterStamp.getByHash(clusterStampInProgressHash);
        clusterStampData.setHash(clusterStampCurrentHash);
        clusterStamp.put(clusterStampData);
        clusterStamp.put(new ClusterStampData(clusterStampInProgressHash));
        propagationPublisher.propagate(clusterStampData, Arrays.asList(NodeType.DspNode));

        log.info("Restart dsp vote service to sum and save dsp votes");
        dspVoteService.startSumAndSaveVotes();
    }
}