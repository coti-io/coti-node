package io.coti.nodemanager.services;

import io.coti.basenode.crypto.SetNewClusterStampsRequestCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.exceptions.ClusterStampValidationException;
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private static final int NUMBER_OF_INITIAL_SIGNATURE_LINES = 2;

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
                    setNewClusterStampsRequest.getCurrencyClusterStampFileName(), setNewClusterStampsRequest.getBalanceClusterStampFileName());
        } catch (Exception e) {
            log.error("Error at getting new cluster stamp files candidates" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }

        SetNewClusterStampsResponse setNewClusterStampsResponse = new SetNewClusterStampsResponse();
        return ResponseEntity.status(HttpStatus.CREATED).body(setNewClusterStampsResponse);
    }

    private void handlePotentialNewClusterStampFile(String candidateClusterStampBucketName, String currencyClusterStampFileName, String balanceClusterStampFileName) {
        try {
            ClusterStampNameData currencyClusterStampNameData = validateNameAndGetCandidateClusterStampNameData(currencyClusterStampFileName);
            ClusterStampNameData balanceClusterStampNameData = validateNameAndGetCandidateClusterStampNameData(balanceClusterStampFileName);

            downloadSingleClusterStampCandidate(candidateClusterStampBucketName, currencyClusterStampFileName);
            downloadSingleClusterStampCandidate(candidateClusterStampBucketName, balanceClusterStampFileName);

            Map<Hash, CurrencyData> currencyMap = new HashMap<>();
            loadCurrencyClusterStamp(currencyClusterStampName, currencyMap, shouldUpdateClusterStampDBVersion(), true, true);

            Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap = new HashMap<>();
            loadBalanceClusterStamp(balanceClusterStampName, clusterStampCurrencyMap, true, true);

            validateMajorityVotesForClusterStampFilesHashes();

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

    private boolean validateMajorityVotesForClusterStampFilesHashes() {
        //Validate Consensus, compare votes match hashes and that a true majority was reached
//        GeneralVoteResult generalVoteResult = getGeneralVoteResult(); // check
//        Hash candidateCurrencyClusterStampHash = getCandidateCurrencyClusterStampHash();
//        Hash candidateBalanceClusterStampHash = getCandidateBalanceClusterStampHash();
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

    @Override
    protected void handleClusterStampWithoutVotes(ClusterStampData clusterStampData, String clusterStampFileLocation, AtomicInteger signatureRelevantLines) {
        if (signatureRelevantLines.intValue() > NUMBER_OF_INITIAL_SIGNATURE_LINES) {
            throw new ClusterStampValidationException(String.format("Votes can not be added to a file with signature lines exceeding the expected 2 at clusterstamp file %s", clusterStampFileLocation));
        }
        GeneralVoteResult generalVoteResult = getGeneralVoteResult();
        try (FileWriter clusterStampFileWriter = new FileWriter(clusterStampFileLocation, true);
             BufferedWriter clusterStampBufferedWriter = new BufferedWriter(clusterStampFileWriter)) {
            clusterStampBufferedWriter.newLine();
            clusterStampBufferedWriter.append("# Votes");
            clusterStampBufferedWriter.newLine();
            for (GeneralVote generalVote : generalVoteResult.getHashToVoteMapping().values()) {
                writeGeneralVoteDetails(clusterStampBufferedWriter, generalVote);
            }
        } catch (Exception e) {
            throw new ClusterStampValidationException("Exception at clusterstamp signing.", e);
        }
    }

    private void writeGeneralVoteDetails(BufferedWriter clusterStampBufferedWriter, GeneralVote generalVote) throws IOException {
        clusterStampBufferedWriter.append("k," + generalVote.getVoterHash());
        clusterStampBufferedWriter.newLine();
        clusterStampBufferedWriter.append("b," + generalVote.isVoteValid());
        clusterStampBufferedWriter.newLine();
        clusterStampBufferedWriter.append("r," + generalVote.getSignature().getR());
        clusterStampBufferedWriter.newLine();
        clusterStampBufferedWriter.append("s," + generalVote.getSignature().getS());
        clusterStampBufferedWriter.newLine();
    }
}
