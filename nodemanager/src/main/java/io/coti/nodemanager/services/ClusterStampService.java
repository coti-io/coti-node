package io.coti.nodemanager.services;

import io.coti.basenode.crypto.SetNewClusterStampsRequestCrypto;
import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.data.Hash;
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
            handlePotentialNewClusterStampFile(setNewClusterStampsRequest.getFolderPath(), setNewClusterStampsRequest.getClusterStampFileName(),
                    setNewClusterStampsRequest.getExpectedClusterStampHash());
        } catch (Exception e) {
            log.error("Error at getting new cluster stamp files candidates" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }

        SetNewClusterStampsResponse setNewClusterStampsResponse = new SetNewClusterStampsResponse();
        return ResponseEntity.status(HttpStatus.CREATED).body(setNewClusterStampsResponse);
    }

    private void handlePotentialNewClusterStampFile(String candidateClusterStampBucketName, String clusterStampFileName, Hash expectedClusterStampHash) {
        try {
            ClusterStampNameData clusterStampNameData = validateNameAndGetCandidateClusterStampNameData(clusterStampFileName, expectedClusterStampHash);
            downloadSingleClusterStampCandidate(candidateClusterStampBucketName, clusterStampFileName);
            loadClusterStamp(clusterStampName, shouldUpdateClusterStampDBVersion(), true);
            String clusterStampTargetFilePath = clusterStampFolder + getClusterStampFileName(clusterStampNameData);
            fileSystemService.renameFile(clusterStampFolder + clusterStampFileName, clusterStampTargetFilePath);
            awsService.uploadFileToS3(clusterStampBucketName, clusterStampFolder + getClusterStampFileName(clusterStampNameData));
        } catch (ClusterStampException e) {
            throw new ClusterStampException(String.format("Errors on processing clusterstamp files %s loading.%n", clusterStampFileName) + e.getMessage(), e);
        } catch (Exception e) {
            throw new ClusterStampException(String.format("Errors on processing clusterstamp files %s loading.", clusterStampFileName), e);
        }
    }

    private void downloadSingleClusterStampCandidate(String candidateClusterStampBucketName, String clusterStampFileName) {
        String clusterStampFilePath = clusterStampFolder + clusterStampFileName;
        try {
            awsService.downloadFile(clusterStampFilePath, candidateClusterStampBucketName);
        } catch (IOException e) {
            throw new ClusterStampException(String.format("Couldn't download clusterstamp file %s.", clusterStampFileName), e);
        }
    }

}
