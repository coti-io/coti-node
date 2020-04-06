package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import reactor.core.publisher.FluxSink;

import java.util.Map;

public interface ICurrencyService {

    void init();

    void updateCurrencies();

    CurrencyData getNativeCurrency();

    Hash getNativeCurrencyHash();

    CurrencyData getCurrencyData(TransactionData transactionData);

    void putCurrencyData(CurrencyData currencyData);

    void getUpdatedCurrencyBatch(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest, FluxSink<CurrencyData> fluxSink);

    CurrencyData getCurrencyFromDB(Hash currencyHash);

    void generateNativeCurrency();

    void updateCurrenciesFromClusterStamp(Map<Hash, CurrencyData> clusterStampCurrenciesMap, Hash genesisAddress);

    void handleExistingTransaction(TransactionData transactionData);

    void handleMissingTransaction(TransactionData transactionData);

    boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData);

    void addConfirmedCurrency(TransactionData transactionData);
}
