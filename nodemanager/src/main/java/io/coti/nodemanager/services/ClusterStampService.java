package io.coti.nodemanager.services;

import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.data.Hash;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Override
    public void clusterStampExecute(StateMessage stateMessage, StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload) {
// todo
    }

    public ResponseEntity<IResponse> setNewClusterStamps(SetNewClusterStampsRequest setNewClusterStampsRequest) {
        try {
            if (!setNewClusterStampsRequestCrypto.verifySignature(setNewClusterStampsRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            handlePotentialNewClusterStampFile(setNewClusterStampsRequest.getFolderPath(), setNewClusterStampsRequest.getClusterStampFileName(),
                    setNewClusterStampsRequest.getExpectedClusterStampHash(), setNewClusterStampsRequest.getSignerHash());
        } catch (Exception e) {
            log.error("Error at getting new cluster stamp files candidates" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }

        SetNewClusterStampsResponse setNewClusterStampsResponse = new SetNewClusterStampsResponse(setNewClusterStampsRequest.getClusterStampFileName());
        return ResponseEntity.status(HttpStatus.CREATED).body(setNewClusterStampsResponse);
    }

    private void handlePotentialNewClusterStampFile(String candidateClusterStampBucketName, String clusterStampFileName, Hash expectedClusterStampHash, Hash signerHash) {
        try {
            ClusterStampNameData clusterStampNameData = validateNameAndGetCandidateClusterStampNameData(clusterStampFileName, signerHash);
            downloadSingleCandidateClusterStamp(candidateClusterStampBucketName, clusterStampFileName);
            String clusterStampFileNameFinal = getClusterStampFileName(clusterStampNameData);
            String clusterStampFilePath = candidateClusterStampFolder + clusterStampFileNameFinal;
            fileSystemService.renameFile(candidateClusterStampFolder + clusterStampFileName, clusterStampFilePath);
            clusterStampName = clusterStampNameData;
            loadClusterStamp(clusterStampNameData, candidateClusterStampFolder, false, true, false);
            Hash candidateClusterStampHash = getCandidateClusterStampHash();
            if (!candidateClusterStampHash.equals(expectedClusterStampHash)) {
                throw new ClusterStampValidationException(String.format("Bad candidate cluster stamp file name: %s. expected hash %s failed to match calculated hash %s.", clusterStampFileName, expectedClusterStampHash, candidateClusterStampHash));
            }
            String clusterStampTargetFilePath = clusterStampFolder + clusterStampFileNameFinal;
            fileSystemService.renameFile(candidateClusterStampFolder + clusterStampFileNameFinal, clusterStampTargetFilePath);
            awsService.uploadFileToS3(clusterStampBucketName, clusterStampFolder + clusterStampFileNameFinal);
        } catch (ClusterStampException e) {
            throw new ClusterStampException(String.format("Errors on processing clusterstamp files %s loading.%n", clusterStampFileName) + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Errors on processing clusterstamp files %s loading.", clusterStampFileName), e);
        }
    }

    private void downloadSingleCandidateClusterStamp(String candidateClusterStampBucketName, String clusterStampFileName) {
        String clusterStampFilePath = candidateClusterStampFolder + clusterStampFileName;
        try {
            awsService.downloadFile(clusterStampFilePath, candidateClusterStampBucketName);
        } catch (IOException e) {
            throw new ClusterStampException(String.format("Couldn't download clusterstamp file %s.", clusterStampFileName), e);
        }
    }

}
