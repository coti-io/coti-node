package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.OriginatorCurrencyData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTokenDetailsRequest;
import io.coti.basenode.http.GetUserTokensRequest;
import io.coti.basenode.http.interfaces.IResponse;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

public interface ICurrencyService {

    void init();

    CurrencyData getNativeCurrency();

    Hash getNativeCurrencyHash();

    CurrencyData getCurrencyData(TransactionData transactionData);

    void putCurrencyData(CurrencyData currencyData);

    CurrencyData getCurrencyFromDB(Hash currencyHash);

    void generateNativeCurrency();

    void updateCurrenciesFromClusterStamp(Map<Hash, CurrencyData> clusterStampCurrenciesMap);

    void handleExistingTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    void validateName(OriginatorCurrencyData originatorCurrencyData);

    void validateSymbol(OriginatorCurrencyData originatorCurrencyData);

    boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData);

    void addConfirmedCurrency(TransactionData transactionData);

    ResponseEntity<IResponse> getUserTokens(GetUserTokensRequest getUserTokensRequest);

    void putToMintableAmountMap(Hash tokenHash, BigDecimal amount);

    BigDecimal getTokenMintableAmount(Hash tokenHash);

    ResponseEntity<IResponse> getTokenDetails(GetTokenDetailsRequest getTokenDetailsRequest);
}
