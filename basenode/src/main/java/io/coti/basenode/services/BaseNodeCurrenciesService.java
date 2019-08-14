package io.coti.basenode.services;

import io.coti.basenode.crypto.GetCurrenciesRequestCrypto;
import io.coti.basenode.crypto.GetCurrenciesResponseCrypto;
import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetCurrenciesRequest;
import io.coti.basenode.http.GetCurrenciesResponse;
import io.coti.basenode.model.Currencies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class BaseNodeCurrenciesService {

    @Autowired
    private Currencies currencies;
    @Autowired
    private GetCurrenciesRequestCrypto getCurrenciesRequestCrypto;
    @Autowired
    private GetCurrenciesResponseCrypto getCurrenciesResponseCrypto;

    public void init() {
        //TODO 8/13/2019 tomer: Consider if we need to keep all in memory like balanceMap
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public ResponseEntity<GetCurrenciesResponse> getMissingCurrencies(GetCurrenciesRequest getCurrenciesRequest) {
        if (!getCurrenciesRequestCrypto.verifySignature(getCurrenciesRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GetCurrenciesResponse());
        }
        Set<Hash> existingCurrenciesHashes = getCurrenciesRequest.getCurrenciesHashes();
        GetCurrenciesResponse getCurrenciesResponse = new GetCurrenciesResponse();
        Set<CurrencyData> currencyDataSet = new HashSet<>();

        if (existingCurrenciesHashes.isEmpty()) {
            currencies.forEach(storedCurrencyData -> {
                currencyDataSet.add(storedCurrencyData);
            });
        } else {
            currencies.forEach(storedCurrencyData -> {
                if (!existingCurrenciesHashes.contains(storedCurrencyData.getHash())) {
                    currencyDataSet.add(storedCurrencyData);
                }
            });
        }
        getCurrenciesResponse.setCurrencyDataSet(currencyDataSet);
        getCurrenciesResponseCrypto.signMessage(getCurrenciesResponse);
        return ResponseEntity.status(HttpStatus.OK).body(getCurrenciesResponse);
    }

    public Set<Hash> getExistingCurrencyHashes() {
        Set<Hash> existingCurrenciesHashes = new HashSet<>();
        currencies.forEach(storedCurrencyData -> {
            existingCurrenciesHashes.add(storedCurrencyData.getHash());
        });
        return existingCurrenciesHashes;
    }

}
