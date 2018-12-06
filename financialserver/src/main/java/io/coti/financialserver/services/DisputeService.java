package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.crypto.DisputeCrypto;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.http.NewDisputeRequest;
import io.coti.financialserver.http.NewDisputeResponse;
import io.coti.financialserver.model.Disputes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class DisputeService {

    @Autowired
    private DisputeCrypto disputeCrypto;
    @Autowired
    Disputes disputes;

    public ResponseEntity<IResponse> createDispute(NewDisputeRequest newDisputeRequest) {
        DisputeData disputeData = newDisputeRequest.getDisputeData();

        disputeCrypto.signMessage(disputeData);

        if (!disputeCrypto.verifySignature(disputeData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(DISPUTE_UNAUTHORIZED, STATUS_ERROR));
        }

        if (isDisputeExist(disputeData.getHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(DISPUTE_ALREADY_EXISTS, STATUS_ERROR));
        }

        disputes.put(disputeData);

        return ResponseEntity.status(HttpStatus.OK).body(new NewDisputeResponse(disputeData.getHash().toString(), STATUS_SUCCESS));
    }

    public ResponseEntity getDispute(Hash userHash, Hash disputeHash) {
        DisputeData disputeData = disputes.getByHash(disputeHash);

        if (disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DISPUTE_NOT_FOUND);
        }

        if (!disputeData.getConsumerHash().toString().equals(userHash.toString())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DISPUTE_UNAUTHORIZED);
        }

        return ResponseEntity.status(HttpStatus.OK).body(disputeData);
    }

    private Boolean isDisputeExist(Hash disputeHash) {
        return (disputes.getByHash(disputeHash) != null);
    }
}
