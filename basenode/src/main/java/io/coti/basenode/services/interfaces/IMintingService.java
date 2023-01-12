package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTokenMintingFeeQuoteRequest;
import io.coti.basenode.http.TokenMintingFeeRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

public interface IMintingService {

    void init();

    boolean checkMintingAmountAndUpdateMintableAmount(TransactionData transactionData);

    void revertMintingAllocation(TransactionData transactionData);

    void handleExistingTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    void doTokenMinting(TransactionData transactionData);

    ResponseEntity<IResponse> getTokenMintingFeeQuote(GetTokenMintingFeeQuoteRequest getTokenMintingFeeQuoteRequest);

    ResponseEntity<IResponse> getTokenMintingFee(TokenMintingFeeRequest tokenMintingFeeRequest);
}
