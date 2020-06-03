package io.coti.nodemanager.services;

import io.coti.basenode.crypto.SetNewClusterStampsRequestCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.SetNewClusterStampsRequest;
import io.coti.basenode.http.SetNewClusterStampsResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Autowired
    private SetNewClusterStampsRequestCrypto setNewClusterStampsRequestCrypto;

    @Override
    public void clusterStampExecute(StateMessage stateMessage, StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload) {
// todo
    }

    public ResponseEntity<IResponse> setNewClusterStamps(SetNewClusterStampsRequest setNewClusterStampsRequest) {
        try {
            if (!setNewClusterStampsRequestCrypto.verifySignature(setNewClusterStampsRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            handlePotentialNewClusterStampFile(setNewClusterStampsRequest.getFolderPath(),
                    setNewClusterStampsRequest.getCurrencyClusterStampFileName(), setNewClusterStampsRequest.getBalanceClusterStampFileName(),
                    setNewClusterStampsRequest.getGeneralVoteResultHash());
        } catch (Exception e) {
            log.error("Error at getting new cluster stamp files candidates" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }

        SetNewClusterStampsResponse setNewClusterStampsResponse = new SetNewClusterStampsResponse();
        return ResponseEntity.status(HttpStatus.CREATED).body(setNewClusterStampsResponse);
    }

    private void handlePotentialNewClusterStampFile(String candidateClusterStampBucketName, String currencyClusterStampFileName,
                                                    String balanceClusterStampFileName, Hash generalVoteResultHash) {
        try {
            ClusterStampNameData currencyClusterStampNameData = validateNameAndGetCandidateClusterStampNameData(currencyClusterStampFileName);
            ClusterStampNameData balanceClusterStampNameData = validateNameAndGetCandidateClusterStampNameData(balanceClusterStampFileName);

            downloadSingleClusterStampCandidate(candidateClusterStampBucketName, currencyClusterStampFileName);
            downloadSingleClusterStampCandidate(candidateClusterStampBucketName, balanceClusterStampFileName);

            Map<Hash, CurrencyData> currencyMap = new HashMap<>();
            loadCurrencyClusterStamp(currencyClusterStampFileName, currencyMap, shouldUpdateClusterStampDBVersion(), true);

            Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap = new HashMap<>();
            loadBalanceClusterStamp(balanceClusterStampFileName, clusterStampCurrencyMap, true);

            List<Hash> allCurrentValidators = networkService.getCurrentValidators();

            if (!validateMajorityVotesForClusterStampFilesHashes(currencyClusterStampFileName, generalVoteResultHash, allCurrentValidators)) {
                throw new ClusterStampException(String.format("Errors during cluster stamp %s votes validation", currencyClusterStampFileName));
            }

            if (!validateMajorityVotesForClusterStampFilesHashes(balanceClusterStampFileName, generalVoteResultHash, allCurrentValidators)) {
                throw new ClusterStampException(String.format("Errors during cluster stamp %s votes validation", balanceClusterStampFileName));
            }

            String currencyClusterStampTargetFilePath = clusterStampFolder + getClusterStampFileName(currencyClusterStampNameData);
            fileSystemService.renameFile(clusterStampFolder + currencyClusterStampFileName, currencyClusterStampTargetFilePath);

            String balanceClusterStampTargetFilePath = clusterStampFolder + getClusterStampFileName(balanceClusterStampNameData);
            fileSystemService.renameFile(clusterStampFolder + balanceClusterStampFileName, balanceClusterStampTargetFilePath);

            awsService.uploadFileToS3(clusterStampBucketName, clusterStampFolder + getClusterStampFileName(currencyClusterStampNameData));
            awsService.uploadFileToS3(clusterStampBucketName, clusterStampFolder + getClusterStampFileName(balanceClusterStampNameData));

        } catch (ClusterStampException e) {
            throw new ClusterStampException(String.format("Errors on processing clusterstamp files %s %s loading.%n", currencyClusterStampFileName, balanceClusterStampFileName) + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Errors on processing clusterstamp files %s %s loading.", currencyClusterStampFileName, balanceClusterStampFileName), e);
        }
    }

    private boolean validateMajorityVotesForClusterStampFilesHashes(String clusterStampFileName, Hash generalVoteResultHash, List<Hash> allCurrentValidators) {
        //Validate Consensus, compare votes match hashes and that a true majority was reached
        GeneralVoteResult generalVoteResult = getGeneralVoteResult(); // check
        Hash candidateCurrencyClusterStampHash = getCandidateCurrencyClusterStampHash();
        Hash candidateBalanceClusterStampHash = getCandidateBalanceClusterStampHash();

//        allCurrentValidators
        Map<Hash, GeneralVote> votesFromClusterStamp = getVotesFromClusterStamp(clusterStampFileName, generalVoteResultHash);


        return true;
    }

    private void downloadSingleClusterStampCandidate(String candidateClusterStampBucketName, String currencyClusterStampFileName) {
        String currencyClusterStampFilePath = clusterStampFolder + currencyClusterStampFileName;
        try {
            awsService.downloadFile(currencyClusterStampFilePath, candidateClusterStampBucketName);
        } catch (IOException e) {
            throw new ClusterStampException(String.format("Couldn't download clusterstamp file %s.", currencyClusterStampFileName), e);
        }
    }

}
