package io.coti.zerospend.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.data.messages.StateMessageClusterStampHashPayload;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Autowired
    private IClusterStampService clusterStampService;
    private static final int NUMBER_OF_RESENDS = 3;

    @Override
    protected void continueHandleGeneralVoteMessage(boolean consensusReached, GeneralVoteMessage generalVoteMessage) {
        if (!consensusReached) {
            return;
        }
        switch (generalVoteMessage.getMessagePayload().getGeneralMessageType()) {
            case CLUSTER_STAMP_INDEX_VOTE:
                //todo calculate clusterstampdata and hashes  - Tomer
                clusterStampService.prepareCandidateClusterStampHash();
                Hash clusterStampHash = clusterStampService.getCandidateClusterStampHash();

                StateMessageClusterStampHashPayload stateMessageClusterStampBalanceHashPayload = new StateMessageClusterStampHashPayload(clusterStampHash);
                StateMessage stateBalanceHashMessage = new StateMessage(stateMessageClusterStampBalanceHashPayload);
                stateBalanceHashMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateBalanceHashMessage)));
                generalMessageCrypto.signMessage(stateBalanceHashMessage);
                startCollectingVotes(stateBalanceHashMessage);
//                //TODO 6/11/2020 tomer: Need to align exact messages
//                clusterStampService.updateGeneralVoteMessageClusterStampSegment(true, stateBalanceHashMessage);

                log.info(String.format("Initiate voting for the balance clusterstamp hash %s %s", clusterStampHash.toHexString(), stateBalanceHashMessage.getHash().toString()));
                propagationPublisher.propagate(stateBalanceHashMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));

//                StateMessageClusterStampHashPayload stateMessageClusterStampCurrencyHashPayload = new StateMessageClusterStampHashPayload(clusterStampCurrencyHash);
//                StateMessage stateCurrencyHashMessage = new StateMessage(stateMessageClusterStampCurrencyHashPayload);
//                stateCurrencyHashMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateCurrencyHashMessage)));
//                generalMessageCrypto.signMessage(stateCurrencyHashMessage);
//                startCollectingVotes(stateCurrencyHashMessage);
//                log.info(String.format("Initiate voting for the currency clusterstamp hash %s %s", clusterStampCurrencyHash.toHexString(), stateCurrencyHashMessage.getHash().toString()));
//                propagationPublisher.propagate(stateCurrencyHashMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));

                for (int i = 0; i < NUMBER_OF_RESENDS; i++) {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ignored) {
                        // ignored exception
                    }
                    propagationPublisher.propagate(stateBalanceHashMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
//                    propagationPublisher.propagate(stateCurrencyHashMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
                }
                break;
            case CLUSTER_STAMP_HASH_VOTE:
                //todo start clusterstamp

                StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload = new StateMessageClusterStampExecutePayload(generalVoteMessage.getVoteHash());
                StateMessage stateMessageExecute = new StateMessage(stateMessageClusterStampExecutePayload);
                stateMessageExecute.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateMessageExecute)));
                generalMessageCrypto.signMessage(stateMessageExecute);
                log.info("Initiate clusterstamp execution " + generalVoteMessage.getVoteHash().toString());
                propagationPublisher.propagate(stateMessageExecute, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));

                for (int i = 0; i < NUMBER_OF_RESENDS; i++) {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception ignored) {
                        // ignored exception
                    }
                    propagationPublisher.propagate(stateMessageExecute, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
                }

                //todo clean clusterStampService.clusterstampdata
                //todo start DB cleaning
                break;
            default:
                log.error("Unexpected vote type: {}", generalVoteMessage.getMessagePayload().getGeneralMessageType());
        }


    }
}
