package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.OriginatorCurrencyData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.*;
import io.coti.basenode.http.data.TokenResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

public interface ICurrencyService {

    void init();

    Hash getNativeCurrencyHash();

    boolean isCurrencyHashAllowed(Hash currencyHash);

    CurrencyData getCurrencyDataFromDB(OriginatorCurrencyData originatorCurrencyData);

    BigDecimal getPostponedMintingAmount(Hash tokenHash);

    void updateMintableAmountMapAndBalance(TransactionData transactionData);

    void synchronizedUpdateMintableAmountMapAndBalance(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData);

    void addConfirmedCurrency(TransactionData transactionData);

    ResponseEntity<IResponse> getUserTokens(GetUserTokensRequest getUserTokensRequest);

    void putToMintableAmountMap(Hash tokenHash, BigDecimal amount);

    BigDecimal getTokenMintableAmount(Hash tokenHash);

    ResponseEntity<IResponse> getTokenDetails(GetTokenDetailsRequest getTokenDetailsRequest);

    ResponseEntity<IResponse> getTokenSymbolDetails(GetTokenSymbolDetailsRequest getTokenSymbolDetailsRequest);

    void handleExistingTransaction(TransactionData transactionData);

    boolean isNativeCurrency(Hash currencyHash);

    Hash getNativeCurrencyHashIfNull(Hash currencyHash);

    TokenResponseData fillTokenGenerationResponseData(Hash currencyHash);

    ResponseEntity<IResponse> getTokenHistory(GetTokenHistoryRequest getTokenHistoryRequest);

    void revertCurrencyUnconfirmedRecord(TransactionData transactionData);

    ResponseEntity<IResponse> getTokenGenerationFee(GenerateTokenFeeRequest generateTokenFeeRequest);

    ResponseEntity<IResponse> getCurrenciesForWallet(GetCurrenciesRequest getCurrenciesRequest);
}
