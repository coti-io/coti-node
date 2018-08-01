package io.coti.zerospend.services;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
import io.coti.common.http.GetZeroSpendTransactionsRequest;
import io.coti.zerospend.monitor.interfaces.IAccessMonitor;
import io.coti.zerospend.services.interfaces.IGetZeroSpendTrxService;
import io.coti.zerospend.services.interfaces.ITransactionIndexerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class GetZeroSpendTrxService implements IGetZeroSpendTrxService {


    private final Hash baseTransactionAddressHash = new Hash("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF\" +\n" +
            "               \"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
    private final Hash baseTransactionHash = new Hash("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB" +
            "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
    private final SignatureData signatureData = new SignatureData("", "");
    private final Hash transactionHash = new Hash("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
    private final String zeroSpendDescription = "zero spend";
    private final BigDecimal zeroSpendAmount = new BigDecimal(-100);
    @Autowired
    private IAccessMonitor monitorAccess;

    @Autowired
    private ITransactionIndexerService transactionIndexerService;

    @Override
    public ResponseEntity<TransactionData> generateZeroSpendTrx(GetZeroSpendTransactionsRequest getZeroSpendTransactionsRequest) {

        if (!monitorAccess.validateAccessEvent(getZeroSpendTransactionsRequest.getFullNodeHash())) {
            TransactionData badTransactionData = new TransactionData(new LinkedList<>(), transactionHash,
                    "The node reached the limit of requests ", -1, new Date());
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(badTransactionData);
        }

        TransactionData trxData = getZeroSpendTransactionsRequest.getTransactionData();
        BaseTransactionData baseTransactionData = new BaseTransactionData(baseTransactionAddressHash,
                zeroSpendAmount, baseTransactionHash, signatureData, new Date());
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.add(baseTransactionData);
        TransactionData transactionData = new TransactionData(baseTransactionDataList, transactionHash,
                zeroSpendDescription, trxData.getSenderTrustScore(), new Date());
        transactionIndexerService.generateAndSetTransactionIndex(transactionData);

        return ResponseEntity.status(HttpStatus.OK).body(transactionData);
    }
}
