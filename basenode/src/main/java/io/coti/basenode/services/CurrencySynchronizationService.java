package io.coti.basenode.services;

import io.coti.basenode.crypto.GetCurrenciesRequestCrypto;
import io.coti.basenode.crypto.GetCurrenciesResponseCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetCurrenciesRequest;
import io.coti.basenode.http.GetCurrenciesResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class CurrencySynchronizationService {

    private static final String RECOVERY_NODE_GET_CURRENCIES_ENDPOINT = "/currencies";

    @Autowired
    private BaseNodeCurrenciesService baseNodeCurrenciesService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private Currencies currencies;
    @Autowired
    private GetCurrenciesRequestCrypto getCurrenciesRequestCrypto;
    @Autowired
    private GetCurrenciesResponseCrypto getCurrenciesResponseCrypto;

    public void requestMissingCurrencies() {
        Set<Hash> existingCurrencyHashes = baseNodeCurrenciesService.getExistingCurrencyHashes();
        GetCurrenciesRequest getCurrenciesRequest = new GetCurrenciesRequest();
        getCurrenciesRequest.setCurrenciesHashes(existingCurrencyHashes);
        getCurrenciesRequestCrypto.signMessage(getCurrenciesRequest);

        HttpEntity<GetCurrenciesRequest> entity = new HttpEntity<>(getCurrenciesRequest);
        ResponseEntity<GetCurrenciesResponse> getCurrenciesResponseEntity =
                restTemplate.postForEntity(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_CURRENCIES_ENDPOINT,
                 entity, GetCurrenciesResponse.class);

        HttpStatus statusCode = getCurrenciesResponseEntity.getStatusCode();
        if (!statusCode.equals(HttpStatus.OK)) {
            log.error("Error at getting missing currencies from recovery server");
        } else {
            GetCurrenciesResponse getCurrenciesResponse = getCurrenciesResponseEntity.getBody();
            if(getCurrenciesResponseCrypto.verifySignature(getCurrenciesResponse)) {
                getCurrenciesResponse.getCurrencyDataSet().forEach(currencyData -> {  currencies.put(currencyData); });
            } else {
                log.error("Authorization error at getting missing currencies from recovery server");
            }
        }
    }
}
