package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
import io.coti.basenode.http.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

        GeneralVoteMessage generalVoteMessage = createGeneralVoteMessage(createTime, clusterStampDataMessageHash);

        updateGeneralVoteMessageClusterStampSegment(prepareClusterStampLines, generalVoteMessage);

        writeClusterStamp(createTime);
        uploadClusterStamp = true;
    }

    private void createNewClusterStampFile() {
        // Create cluster stamp hash
        boolean prepareClusterStampLines = true;
        Hash clusterStampDataMessageHash = getCandidateClusterStampHash();

        // Update with voters snapshot
        GetNetworkVotersResponse getNetworkVotersResponse = getNetworkVoters();
        if (!getNetworkVotersCrypto.verifySignature(getNetworkVotersResponse)) {
            throw new ClusterStampValidationException(String.format("Network validators Clusterstamp file %s failed signature", getClusterStampFileName(clusterStampName)));
        }
        String voterNodesDetails = Base64.getEncoder().encodeToString(SerializationUtils.serialize(getNetworkVotersResponse));
        updateClusterStampVoterNodesDetails(voterNodesDetails);

        validatorsVoteClusterStampSegmentLines = new ArrayList<>();
        // For each vote
        GeneralVoteMessage generalVoteMessage = createGeneralVoteMessage(clusterStampCreateTime, clusterStampDataMessageHash);
        updateGeneralVoteMessageClusterStampSegment(prepareClusterStampLines, generalVoteMessage);

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

    @Override
    protected boolean isMissingSegmentsAllowed() {
        return true;
    }

    @Override
    protected boolean isUpdateNativeCurrencyFromClusterStamp() {
        return false;
    }

}