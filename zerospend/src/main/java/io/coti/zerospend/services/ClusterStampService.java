package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.StateMessageCrypto;
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
import io.coti.basenode.services.interfaces.IVoteService;
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
    private StateMessageCrypto stateMessageCrypto;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IVoteService voteService;
    @Autowired
    private DspVoteService dspVoteService;

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
            uploadCandidateClusterStamp(clusterStampName);
        }
    }

    private void uploadCandidateClusterStamp(ClusterStampNameData clusterStampNameData) {
        log.info("Starting to upload clusterstamp");
        String candidateClusterStampFileName = getCandidateClusterStampFileName(clusterStampNameData);
        uploadCandidateClusterStamp(candidateClusterStampFileName);
        SetNewClusterStampsRequest setNewClusterStampsRequest =
                new SetNewClusterStampsRequest(candidateClusterStampBucketName, candidateClusterStampFileName, getCandidateClusterStampHash());

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

        VoteMessageData generalVoteMessage = createHashVoteMessage(createTime, clusterStampDataMessageHash, clusterStampDataMessageHash);  // todo delete it ?

        updateGeneralVoteMessageClusterStampSegment(prepareClusterStampLines, generalVoteMessage);

        writeClusterStamp(createTime);
        uploadClusterStamp = true;
    }

    private void addVotesAndUploadNewClusterStampFile(List<VoteMessageData> generalVoteMessageList) {
        // Update with voters snapshot
        GetNetworkVotersResponse getNetworkVotersResponse = getNetworkVoters();
        if (!getNetworkVotersCrypto.verifySignature(getNetworkVotersResponse)) {
            throw new ClusterStampValidationException(String.format("Network validators Clusterstamp file %s failed signature", getClusterStampFileName(clusterStampName)));
        }
        String voterNodesDetails = Base64.getEncoder().encodeToString(SerializationUtils.serialize(getNetworkVotersResponse));
        updateClusterStampVoterNodesDetails(voterNodesDetails);

        validatorsVoteClusterStampSegmentLines = new ArrayList<>();
        // For each vote
        generalVoteMessageList.forEach(v -> updateGeneralVoteMessageClusterStampSegment(true, v));

        writeClusterStamp(clusterStampCreateTime);
        String versionTimeMillisString = String.valueOf(clusterStampCreateTime.toEpochMilli());
        candidateClusterStampName = new ClusterStampNameData(versionTimeMillisString, versionTimeMillisString);

        uploadClusterStamp = true;
        uploadCandidateClusterStamp(candidateClusterStampName);
        clearCandidateClusterStampFolder();
        clearClusterStampFolderExceptForSingleFile(candidateClusterStampName);
        clusterStampName = candidateClusterStampName;
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
        InitiateClusterStampStateMessageData initiateClusterStampStateMessageData = new InitiateClusterStampStateMessageData(CLUSTER_STAMP_INITIATED_DELAY, CLUSTER_STAMP_TIMEOUT, Instant.now());
        clusterStampInitiateTimestamp = initiateClusterStampStateMessageData.getCreateTime();
        initiateClusterStampStateMessageData.setHash(new Hash(stateMessageCrypto.getSignatureMessage(initiateClusterStampStateMessageData)));
        stateMessageCrypto.signMessage(initiateClusterStampStateMessageData);
        propagationPublisher.propagate(initiateClusterStampStateMessageData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));
        log.info("Manually initiated clusterstamp" + initiateClusterStampStateMessageData.getHash().toString());

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

        lastConfirmedIndexForClusterStamp = 0;
        while (Instant.now().isBefore(waitForTCCTill)) {
            lastConfirmedIndexForClusterStamp = clusterService.getMaxIndexOfNotConfirmed();
            if (lastConfirmedIndexForClusterStamp == 0) {
                break;
            }
            Thread.sleep(1000);
        }
        if (lastConfirmedIndexForClusterStamp <= 0) {
            lastConfirmedIndexForClusterStamp = transactionIndexService.getLastTransactionIndexData().getIndex();
        }

        LastIndexClusterStampStateMessageData lastIndexClusterStampStateMessageData = new LastIndexClusterStampStateMessageData(lastConfirmedIndexForClusterStamp, Instant.now());
        lastIndexClusterStampStateMessageData.setHash(new Hash(stateMessageCrypto.getSignatureMessage(lastIndexClusterStampStateMessageData)));
        stateMessageCrypto.signMessage(lastIndexClusterStampStateMessageData);
        voteService.startCollectingVotes(lastIndexClusterStampStateMessageData, createLastIndexVoteMessage(Instant.now(), lastIndexClusterStampStateMessageData.getHash()));
        log.info(String.format("Initiate voting for the last clusterstamp index %d %s", lastConfirmedIndexForClusterStamp, lastIndexClusterStampStateMessageData.getHash().toString()));
        propagateRetries(Collections.singletonList(lastIndexClusterStampStateMessageData));
    }

    @Override
    public void calculateClusterStampDataAndHashesAndSendMessage() {
        calculateClusterStampDataAndHashes(clusterStampInitiateTimestamp);  // todo there is no check of clusterStampInitiateTimestamp, but may be it is needed for emergency scenarious
        Hash clusterStampHash = getCandidateClusterStampHash();

        HashClusterStampStateMessageData hashClusterStampStateMessageData = new HashClusterStampStateMessageData(clusterStampHash, Instant.now());
        hashClusterStampStateMessageData.setHash(new Hash(stateMessageCrypto.getSignatureMessage(hashClusterStampStateMessageData)));
        stateMessageCrypto.signMessage(hashClusterStampStateMessageData);
        voteService.startCollectingVotes(hashClusterStampStateMessageData, createHashVoteMessage(Instant.now(), hashClusterStampStateMessageData.getHash(), clusterStampHash));
//                //TODO 6/11/2020 tomer: Need to align exact messages
//                clusterStampService.updateGeneralVoteMessageClusterStampSegment(true, hashClusterStampStateMessageData);

        log.info(String.format("Initiate voting for the balance clusterstamp hash %s %s", clusterStampHash.toHexString(), hashClusterStampStateMessageData.getHash().toString()));

//                StateMessageClusterStampHashPayload stateMessageClusterStampCurrencyHashPayload = new StateMessageClusterStampHashPayload(clusterStampCurrencyHash);
//                StateMessage stateCurrencyHashMessage = new StateMessage(stateMessageClusterStampCurrencyHashPayload);
//                stateCurrencyHashMessage.setHash(new Hash(generalMessageCrypto.getSignatureMessage(stateCurrencyHashMessage)));
//                generalMessageCrypto.signMessage(stateCurrencyHashMessage);
//                startCollectingVotes(stateCurrencyHashMessage);
//                log.info(String.format("Initiate voting for the currency clusterstamp hash %s %s", clusterStampCurrencyHash.toHexString(), stateCurrencyHashMessage.getHash().toString()));
//                propagationPublisher.propagate(stateCurrencyHashMessage, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode, NodeType.NodeManager));

        voteService.clearClusterStampHashVoteDone();
        propagateRetries(Collections.singletonList(hashClusterStampStateMessageData));
//        propagateRetries(Arrays.asList(hashClusterStampStateMessageData, stateCurrencyHashMessage));
    }

    @Override
    public void doClusterStampAfterVoting(Hash voteHash) {

        ContinueClusterStampStateMessageData continueClusterStampStateMessageData = new ContinueClusterStampStateMessageData(Instant.now());
        continueClusterStampStateMessageData.setHash(new Hash(stateMessageCrypto.getSignatureMessage(continueClusterStampStateMessageData)));
        stateMessageCrypto.signMessage(continueClusterStampStateMessageData);
        log.info("Nodes can continue with transaction processing " + voteHash.toString());
        propagateRetries(Collections.singletonList(continueClusterStampStateMessageData));


        addVotesAndUploadNewClusterStampFile(voteService.getVoteResultVotersList(VoteMessageType.CLUSTER_STAMP_HASH_VOTE, voteHash));

        ExecuteClusterStampStateMessageData executeClusterStampStateMessageData = new ExecuteClusterStampStateMessageData(voteHash, lastConfirmedIndexForClusterStamp, Instant.now());
        executeClusterStampStateMessageData.setHash(new Hash(stateMessageCrypto.getSignatureMessage(executeClusterStampStateMessageData)));
        stateMessageCrypto.signMessage(executeClusterStampStateMessageData);
        log.info("Initiate clusterstamp execution " + voteHash.toString());
        clusterStampExecute(executeClusterStampStateMessageData);   //todo may be separate thread
        propagateRetries(Collections.singletonList(executeClusterStampStateMessageData));

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
