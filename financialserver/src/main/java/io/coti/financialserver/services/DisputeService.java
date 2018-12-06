package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.database.RocksDBConnector;
import io.coti.financialserver.data.DisputeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.coti.financialserver.model.Disputes;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class DisputeService {

    private static final String ALREADY_EXIST = "Dispute with this hash already exist";
    private static final String NOT_FOUND = "Not found";
    private static final String NOT_YOURS = "Dispute doesn't belong to you";

    @Autowired
    Disputes disputes;

    public DisputeService() {
        disputes = new Disputes();
        disputes.init();
        disputes.databaseConnector = RocksDBConnector.getConnector();
    }

    public ResponseEntity newDispute(Hash consumerHash, Hash transactionHash, List<DisputeItemData> disputeItems, BigDecimal amount) {

        DisputeData disputeData = new DisputeData(consumerHash, transactionHash, disputeItems, amount);

        if(isDisputeExist(disputeData.getHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ALREADY_EXIST);
        }

        disputes.put(disputeData);

        return ResponseEntity.status(HttpStatus.OK).body(disputeData.getHash().toString());
    }

    public ResponseEntity getDispute(Hash userHash, Hash disputeHash) {
        DisputeData disputeData = disputes.getByHash(disputeHash);

        if(disputeData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(NOT_FOUND);
        }

        if( ! disputeData.getConsumerHash().toString().equals(userHash.toString()) ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(NOT_YOURS);
        }

        return ResponseEntity.status(HttpStatus.OK).body(disputeData);
    }

    private Boolean isDisputeExist(Hash disputeHash) {
        return (disputes.getByHash(disputeHash) != null);
    }
}
