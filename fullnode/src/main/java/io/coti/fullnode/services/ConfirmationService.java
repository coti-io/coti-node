package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TokenMintingFeeBaseTransactionData;
import io.coti.basenode.data.TokenMintingServiceData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.services.BaseNodeConfirmationService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.fullnode.websocket.WebSocketSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Autowired
    private WebSocketSender webSocketSender;
    @Autowired
    private ITransactionHelper transactionHelper;

    @Override
    protected void continueHandleAddressHistoryChanges(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.CONFIRMED);
    }

    @Override
    protected void continueHandleTokenChanges(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = transactionHelper.getTokenMintingFeeData(transactionData);
        if (tokenMintingFeeBaseTransactionData != null) {
            TokenMintingServiceData tokenMintingFeeBaseTransactionServiceData = tokenMintingFeeBaseTransactionData.getServiceData();
            Hash tokenHash = tokenMintingFeeBaseTransactionServiceData.getMintingCurrencyHash();
            webSocketSender.notifyTokenChange(tokenHash);
        }
    }
}
