package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.FluxSink;

import java.math.BigInteger;

public interface ICurrencyService {

    void init();

    void updateCurrencyDataIndexes(CurrencyData currencyData);

    void removeCurrencyDataIndexes(CurrencyData currencyData);

    void updateCurrencies();

    void verifyCurrencyExists(Hash currencyDataHash);

    CurrencyData getNativeCurrency();

    void putCurrencyData(CurrencyData currencyData);

    ResponseEntity<BaseResponse> getUpdatedCurrencies(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest);

    void getUpdatedCurrenciesReactive(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest, FluxSink<CurrencyData> fluxSink);

    BigInteger getTokenTotalSupply(Hash address);
}
