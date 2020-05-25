package io.coti.nodemanager.services;

import io.coti.basenode.crypto.SetNewClusterStampsRequestCrypto;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.SetNewClusterStampsRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Autowired
    private SetNewClusterStampsRequestCrypto setNewClusterStampsRequestCrypto;


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

        return null;
    }

    private void handlePotentialNewClusterStampFile(String folderPath, String currencyClusterStampFileName, String balanceClusterStampFileName) {


        //TODO 5/20/2020 tomer: Validate name

        //TODO 5/20/2020 tomer: Download files

        //TODO 5/20/2020 tomer: Validate contents

        //TODO 5/20/2020 tomer: Validate Consensus

        //TODO 5/20/2020 tomer: Rename files, Add list of signatures at the end of currencies cluster stamp file

        //TODO 5/20/2020 tomer: Upload files to S3

    }
}
