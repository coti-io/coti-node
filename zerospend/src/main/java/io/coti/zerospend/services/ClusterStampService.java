package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.http.GetNetworkVotersResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.ClusterService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;


@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private static final String CLUSTERSTAMP_DELIMITER = ",";
    @Value("${currency.genesis.address}")
    private String currencyAddress;
    @Value("${upload.clusterstamp}")
    private boolean uploadClusterStamp;
    @Autowired
    protected IPropagationPublisher propagationPublisher;

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IGeneralVoteService generalVoteService;
    @Autowired
    private DspVoteService dspVoteService;

    private static final long CLUSTER_STAMP_TIMEOUT = 100;
    private static final long CLUSTER_STAMP_INITIATED_DELAY = 20;
    private static final long CLUSTER_STAMP_WAIT_TCC = 50;
    private static final int NUMBER_OF_RESENDS = 3;

    @Value("${aws.s3.bucket.name.clusterstamp}")
    private void setClusterStampBucketName(String clusterStampBucketName) {
        this.clusterStampBucketName = clusterStampBucketName;
    }

    @Override
    public void init() {
        super.init();
        if (uploadClusterStamp) {
            uploadClusterStamp();
        }
    }

    private void uploadClusterStamp() {
        log.info("Starting to upload clusterstamp");
        uploadClusterStamp(clusterStampName);
        log.info("Finished to upload clusterstamp");
    }

    private void uploadClusterStamp(ClusterStampNameData clusterStampNameData) {
        awsService.uploadFileToS3(clusterStampBucketName, clusterStampFolder + getClusterStampFileName(clusterStampNameData));
    }

    @Override
    protected void fillClusterStampNamesMap() {
        super.fillClusterStampNamesMap();

        Instant now = Instant.now();
        if (clusterStampName == null) {
            handleMissingClusterStamp(now);
        }
    }

    private void handleMissingClusterStamp(Instant createTime) {
        boolean prepareClusterStampLines = true;
        ClusterStampData clusterStampData = new ClusterStampData();
        clearCandidateClusterStampRelatedFields();
        prepareCandidateClusterStampHash(createTime, prepareClusterStampLines, clusterStampData, true);
        Hash clusterStampDataMessageHash = getCandidateClusterStampHash();

        GetNetworkVotersResponse getNetworkVotersResponse = getGetNetworkVotersResponse();
        String voterNodesDetails = Base64.getEncoder().encodeToString(SerializationUtils.serialize(getNetworkVotersResponse));
        updateClusterStampVoterNodesDetails(voterNodesDetails);

        GeneralVoteMessage generalVoteMessage = createGeneralVoteMessage(createTime, clusterStampDataMessageHash);

        validatorsVoteClusterStampSegmentLines = new ArrayList<>();
        updateGeneralVoteMessageClusterStampSegment(prepareClusterStampLines, generalVoteMessage);
        writeClusterStamp(createTime);

        uploadClusterStamp = true;
    }

    private void createClusterStampFile_example(Instant createTime) {
        // Create cluster stamp hash
        boolean prepareClusterStampLines = true;
        ClusterStampData clusterStampData = new ClusterStampData();
        clearCandidateClusterStampRelatedFields();

        prepareCandidateClusterStampHash(createTime, prepareClusterStampLines, clusterStampData, false);
        Hash clusterStampDataMessageHash = getCandidateClusterStampHash();

        // Update with voters snapshot
        GetNetworkVotersResponse getNetworkVotersResponse = getGetNetworkVotersResponse();
        String voterNodesDetails = Base64.getEncoder().encodeToString(SerializationUtils.serialize(getNetworkVotersResponse));
        updateClusterStampVoterNodesDetails(voterNodesDetails);

        validatorsVoteClusterStampSegmentLines = new ArrayList<>();
        // For each vote
        GeneralVoteMessage generalVoteMessage = createGeneralVoteMessage(createTime, clusterStampDataMessageHash);
        updateGeneralVoteMessageClusterStampSegment(prepareClusterStampLines, generalVoteMessage);

        writeClusterStamp(createTime);

        uploadClusterStamp = true;
    }

    @Override
    protected GetNetworkVotersResponse getGetNetworkVotersResponse() {
        return restTemplate.getForEntity(nodeManagerHttpAddress + NODE_MANAGER_VALIDATORS_ENDPOINT, GetNetworkVotersResponse.class).getBody();
    }


    private String generateClusterStampLineFromNewCurrency(ClusterStampData clusterStampData, CurrencyData currencyData) {
        if (currencyAddress == null) {
            throw new ClusterStampException("Unable to start zero spend server. Genesis address not found.");
        }
        StringBuilder sb = new StringBuilder();
        byte[] addressHashInBytes = this.currencyAddress.getBytes();
        byte[] addressCurrencyAmountInBytes = currencyData.getTotalSupply().stripTrailingZeros().toPlainString().getBytes();
        byte[] currencyHashInBytes = currencyData.getHash().getBytes();
        byte[] balanceInBytes = ByteBuffer.allocate(addressHashInBytes.length + addressCurrencyAmountInBytes.length + currencyHashInBytes.length)
                .put(addressHashInBytes).put(addressCurrencyAmountInBytes).put(currencyHashInBytes).array();
        clusterStampData.getSignatureMessage().add(balanceInBytes);
        clusterStampData.incrementMessageByteSize(balanceInBytes.length);
        sb.append(this.currencyAddress).append(CLUSTERSTAMP_DELIMITER).append(currencyData.getTotalSupply().toString()).append(CLUSTERSTAMP_DELIMITER).append(currencyData.getHash());
        return sb.toString();
    }

    @Override
    protected void prepareOnlyForNativeGenesisAddressBalanceClusterStampSegment(ClusterStampData clusterStampData, boolean prepareClusterStampLines, CurrencyData nativeCurrency) {
        if (nativeCurrency == null) {
            throw new ClusterStampException("Unable to start zero spend server. Native token not found.");
        }
        String line = generateClusterStampLineFromNewCurrency(clusterStampData, nativeCurrency);
        if (prepareClusterStampLines) {
            balanceClusterStampSegmentLines.add(line);
        }
    }

    @Override
    protected void handleMissingRecoveryServer() {
        // Zero spend does nothing in this method.
    }

    public ResponseEntity<IResponse> initiateClusterStamp() {
        StateMessageClusterStampInitiatedPayload stateMessageClusterstampInitiatedPayload = new StateMessageClusterStampInitiatedPayload(CLUSTER_STAMP_INITIATED_DELAY, CLUSTER_STAMP_TIMEOUT);
        StateMessage stateMessage = new StateMessage(stateMessageClusterstampInitiatedPayload);
        stateMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateMessage)));
        generalMessageCrypto.signMessage(stateMessage);
        propagationPublisher.propagate(stateMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
        log.info("Manually initiated clusterstamp" + stateMessage.getHash().toString());

        Thread clusterStampCreationThread = new Thread(() -> {
            try {
                clusterStampCreation();
            } catch (InterruptedException e) {
                log.info(String.format("Clusterstump creation interrupted %s", e));
                Thread.currentThread().interrupt();
            }
        });
        clusterStampCreationThread.start();

        dspVoteService.setSumAndSaveVotesPause(); // todo check it is started again
        return ResponseEntity.ok().body(null);
    }

    private void clusterStampCreation() throws InterruptedException {

        Thread.sleep(CLUSTER_STAMP_INITIATED_DELAY * 1000);

        Instant waitForTCCTill = Instant.now().plusSeconds(CLUSTER_STAMP_WAIT_TCC);

        long lastConfirmedIndex = 0;
        while (Instant.now().isBefore(waitForTCCTill)) {
            lastConfirmedIndex = clusterService.getMaxIndexOfNotConfirmed();
            if (lastConfirmedIndex == 0) {
                break;
            }
            Thread.sleep(1000);
        }
        if (lastConfirmedIndex <= 0) {
            lastConfirmedIndex = transactionIndexService.getLastTransactionIndexData().getIndex();
        }

        StateMessageLastClusterStampIndexPayload stateMessageLastClusterStampIndexPayload = new StateMessageLastClusterStampIndexPayload(lastConfirmedIndex);
        StateMessage stateMessage = new StateMessage(stateMessageLastClusterStampIndexPayload);
        stateMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateMessage)));
        generalMessageCrypto.signMessage(stateMessage);
        generalVoteService.startCollectingVotes(stateMessage);
        log.info(String.format("Initiate voting for the last clusterstamp index %d %s", lastConfirmedIndex, stateMessage.getHash().toString()));
        propagateRetries(Collections.singletonList(stateMessage));
    }

    @Override
    public void calculateClusterStampDataAndHashes() {
        //todo calculate clusterstampdata and hashes  - Tomer
        prepareCandidateClusterStampHash();
        Hash clusterStampHash = getCandidateClusterStampHash();

        StateMessageClusterStampHashPayload stateMessageClusterStampBalanceHashPayload = new StateMessageClusterStampHashPayload(clusterStampHash);
        StateMessage stateBalanceHashMessage = new StateMessage(stateMessageClusterStampBalanceHashPayload);
        stateBalanceHashMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateBalanceHashMessage)));
        generalMessageCrypto.signMessage(stateBalanceHashMessage);
        generalVoteService.startCollectingVotes(stateBalanceHashMessage);
//                //TODO 6/11/2020 tomer: Need to align exact messages
//                clusterStampService.updateGeneralVoteMessageClusterStampSegment(true, stateBalanceHashMessage);

        log.info(String.format("Initiate voting for the balance clusterstamp hash %s %s", clusterStampHash.toHexString(), stateBalanceHashMessage.getHash().toString()));

//                StateMessageClusterStampHashPayload stateMessageClusterStampCurrencyHashPayload = new StateMessageClusterStampHashPayload(clusterStampCurrencyHash);
//                StateMessage stateCurrencyHashMessage = new StateMessage(stateMessageClusterStampCurrencyHashPayload);
//                stateCurrencyHashMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateCurrencyHashMessage)));
//                generalMessageCrypto.signMessage(stateCurrencyHashMessage);
//                startCollectingVotes(stateCurrencyHashMessage);
//                log.info(String.format("Initiate voting for the currency clusterstamp hash %s %s", clusterStampCurrencyHash.toHexString(), stateCurrencyHashMessage.getHash().toString()));
//                propagationPublisher.propagate(stateCurrencyHashMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));

        propagateRetries(Collections.singletonList(stateBalanceHashMessage));
//        propagateRetries(Arrays.asList(stateBalanceHashMessage, stateCurrencyHashMessage));
    }

    @Override
    public void doClusterStampAfterVoting(GeneralVoteMessage generalVoteMessage) {
        //todo start clusterstamp

        StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload = new StateMessageClusterStampExecutePayload(generalVoteMessage.getVoteHash());
        StateMessage stateMessageExecute = new StateMessage(stateMessageClusterStampExecutePayload);
        stateMessageExecute.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateMessageExecute)));
        generalMessageCrypto.signMessage(stateMessageExecute);
        log.info("Initiate clusterstamp execution " + generalVoteMessage.getVoteHash().toString());
        propagateRetries(Collections.singletonList(stateMessageExecute));

        //todo clean clusterStampService.clusterstampdata
        //todo start DB cleaning

    }

    private void propagateRetries(List<IPropagatable> messages) {
        for (int i = 0; true; i++) {
            for (IPropagatable message : messages) {
                propagationPublisher.propagate(message, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
            }
            if (i == NUMBER_OF_RESENDS - 1) {
                break;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

}
