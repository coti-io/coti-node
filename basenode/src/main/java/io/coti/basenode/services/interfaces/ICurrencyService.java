package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyNoticeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import reactor.core.publisher.FluxSink;

import java.math.BigDecimal;

public interface ICurrencyService {

    void init();

    void updateCurrencies();

    boolean verifyCurrencyExists(Hash currencyDataHash);

    CurrencyData getNativeCurrency();

    Hash getNativeCurrencyHash();

    void putCurrencyData(CurrencyData currencyData);

    void getUpdatedCurrencyBatch(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest, FluxSink<CurrencyData> fluxSink);

    BigDecimal getTokenTotalSupply(Hash currencyHash);

    int getTokenScale(Hash currencyHash);

    void handlePropagatedCurrencyNotice(CurrencyNoticeData currencyNoticeData);
}
