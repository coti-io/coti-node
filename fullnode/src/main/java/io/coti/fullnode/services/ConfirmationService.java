package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TokenMintingFeeBaseTransactionData;
import io.coti.basenode.data.TokenMintingServiceData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeTransactionHelper;
import static io.coti.fullnode.services.NodeServiceManager.webSocketSender;

@Service
@Primary
public class ConfirmationService extends BaseNodeConfirmationService {

    @Override
    protected void continueHandleAddressHistoryChanges(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.CONFIRMED);
    }

    @Override
    protected void continueHandleTokenChanges(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = nodeTransactionHelper.getTokenMintingFeeData(transactionData);
        TokenMintingServiceData tokenMintingFeeBaseTransactionServiceData = tokenMintingFeeBaseTransactionData.getServiceData();
        Hash tokenHash = tokenMintingFeeBaseTransactionServiceData.getMintingCurrencyHash();
        webSocketSender.notifyTokenChange(tokenHash);
    }
}
