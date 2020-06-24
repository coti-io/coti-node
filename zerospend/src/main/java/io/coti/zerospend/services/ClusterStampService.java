package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.http.*;
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

import java.time.Instant;
import java.util.*;


@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private static final String CLUSTERSTAMP_DELIMITER = ",";
    protected static final String NODE_MANAGER_VALIDATORS_ZEROSPEND_ENDPOINT = "/management/validators/zerospend";
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

    @Override
    public void init() {
        super.init();

        fileSystemService.createFolder(candidateClusterStampFolder);

        if (filledMissingSegments) {
            writeClusterStamp(clusterStampCreateTime);
            uploadClusterStamp = true;
        }
        if (uploadClusterStamp) {
            uploadCandidateClusterStamp();
        }
    }

    private void uploadCandidateClusterStamp() {
        log.info("Starting to upload clusterstamp");
        String candidateClusterStampFileName = getCandidateClusterStampFileName(clusterStampName);
        uploadCandidateClusterStamp(candidateClusterStampFileName);
        SetNewClusterStampsRequest setNewClusterStampsRequest =
                new SetNewClusterStampsRequest(candidateClusterStampBucketName, getCandidateClusterStampFileName(clusterStampName), getCandidateClusterStampHash());

        setNewClusterStampsRequest.setSignerHash(NodeCryptoHelper.getNodeHash());
        setNewClusterStampsRequest.setSignature(NodeCryptoHelper.signMessage(setNewClusterStampsRequestCrypto.getSignatureMessage(setNewClusterStampsRequest)));
        SetNewClusterStampsResponse setNewClusterStampsResponse =
                restTemplate.postForEntity(nodeManagerHttpAddress + NODE_MANAGER_NEW_CLUSTER_STAMP, setNewClusterStampsRequest, SetNewClusterStampsResponse.class).getBody();
        if (!setNewClusterStampsResponse.getStatus().equals(BaseNodeHttpStringConstants.STATUS_SUCCESS)) {
            throw new ClusterStampValidationException(String.format("Failed to upload cluster stamp file: %s to node-manager", setNewClusterStampsResponse.getClusterStampFileName()));
        }
        log.info("Finished to upload clusterstamp");
    }

    @Override
    protected void fillClusterStampNamesMap() {
        super.fillClusterStampNamesMap();

        Instant now = Instant.now();
        if (clusterStampName == null) {
            handleMissingClusterStamp(now);
            super.fillClusterStampNamesMap();
        }
    }

    private void handleMissingClusterStamp(Instant createTime) {
        fileSystemService.createFolder(candidateClusterStampFolder);
        boolean prepareClusterStampLines = true;
        ClusterStampData clusterStampData = new ClusterStampData();
        clearCandidateClusterStampRelatedFields();
        prepareCandidateClusterStampHash(createTime, prepareClusterStampLines, clusterStampData, true);
        Hash clusterStampDataMessageHash = getCandidateClusterStampHash();

        GetNetworkVotersResponse getNetworkVotersResponse = getNetworkVoters();
        if (!getNetworkVotersCrypto.verifySignature(getNetworkVotersResponse)) {
            throw new ClusterStampValidationException(String.format("Network validators Clusterstamp file %s failed signature", getClusterStampFileName(clusterStampName)));
        }
        String voterNodesDetails = Base64.getEncoder().encodeToString(SerializationUtils.serialize(getNetworkVotersResponse));
        updateClusterStampVoterNodesDetails(voterNodesDetails);

        GeneralVoteMessage generalVoteMessage = createGeneralVoteMessage(createTime, clusterStampDataMessageHash);

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
        GetNetworkVotersResponse getNetworkVotersResponse = getNetworkVoters();
        if (getNetworkVotersCrypto.verifySignature(getNetworkVotersResponse)) {
            throw new ClusterStampValidationException(String.format("Network validators Clusterstamp file %s failed signature", getClusterStampFileName(clusterStampName)));
        }
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
    protected GetNetworkVotersResponse getNetworkVoters() {
        NetworkNodeData networkNodeData = networkService.getNetworkNodeData();
        NodeRegistrationData nodeRegistrationData = networkNodeData.getNodeRegistrationData();
        GetNetworkVotersRequest getNetworkVotersRequest = new GetNetworkVotersRequest(nodeRegistrationData);
        return restTemplate.postForEntity(nodeManagerHttpAddress + NODE_MANAGER_VALIDATORS_ZEROSPEND_ENDPOINT, getNetworkVotersRequest, GetNetworkVotersResponse.class).getBody();
    }


    private String generateClusterStampBalanceLineFromNewCurrency(ClusterStampData clusterStampData, CurrencyData currencyData) {
        if (currencyAddress == null) {
            throw new ClusterStampException("Unable to start zero spend server. Genesis address not found.");
        }
        StringBuilder sb = new StringBuilder();
        byte[] addressHashInBytes = new Hash(this.currencyAddress).getBytes();
        byte[] addressCurrencyAmountInBytes = currencyData.getTotalSupply().stripTrailingZeros().toPlainString().getBytes();
        byte[] currencyHashInBytes = currencyData.getHash().getBytes();
        updateClusterStampDataMessageFromBalanceLineDetails(clusterStampData, addressHashInBytes, addressCurrencyAmountInBytes, currencyHashInBytes);
        sb.append(this.currencyAddress).append(CLUSTERSTAMP_DELIMITER).append(currencyData.getTotalSupply().toString()).append(CLUSTERSTAMP_DELIMITER).append(currencyData.getHash());
        return sb.toString();
    }

    @Override
    protected void prepareOnlyForNativeGenesisAddressBalanceClusterStampSegment(ClusterStampData clusterStampData, boolean prepareClusterStampLines, CurrencyData nativeCurrency) {
        if (nativeCurrency == null) {
            throw new ClusterStampException("Unable to start zero spend server. Native token not found.");
        }
        String line = generateClusterStampBalanceLineFromNewCurrency(clusterStampData, nativeCurrency);
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

    @Override
    protected boolean isMissingSegmentsAllowed() {
        return true;
    }

    @Override
    protected boolean isUpdateNativeCurrencyFromClusterStamp() {
        return false;
    }

}
