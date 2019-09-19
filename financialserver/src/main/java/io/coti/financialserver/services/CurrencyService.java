package io.coti.financialserver.services;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.coti.basenode.crypto.GetTokenGenerationDataRequestCrypto;
import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.UserTokenGenerationData;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.http.GenerateTokenRequest;
import io.coti.basenode.http.GetTokenGenerationDataRequest;
import io.coti.basenode.http.GetTokenGenerationDataResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.financialserver.data.CurrencyNameIndexData;
import io.coti.financialserver.data.CurrencySymbolIndexData;
import io.coti.financialserver.model.CurrencyNameIndexes;
import io.coti.financialserver.model.CurrencySymbolIndexes;
import io.coti.financialserver.model.PendingCurrencies;
import io.coti.financialserver.model.UserTokenGenerations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.TOKEN_GENERATION_INVALID_REQUEST;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    private static final String GET_NATIVE_CURRENCY_ENDPOINT = "/currencies/native";
    @Value("${financialserver.seed}")
    private String seed;
    @Autowired
    private CurrencyNameIndexes currencyNameIndexes;
    @Autowired
    private CurrencySymbolIndexes currencySymbolIndexes;
    @Autowired
    protected INetworkService networkService;
    @Autowired
    private GetTokenGenerationDataRequestCrypto getTokenGenerationDataRequestCrypto;
    @Autowired
    private Transactions transactions;
    @Autowired
    private UserTokenGenerations userTokenGenerations;
    @Autowired
    private PendingCurrencies pendingCurrencies;

    private Set<Hash> concurrentUserHashes;
    private Set<String> concurrentCurrencySymbols;
    private Set<String> concurrentCurrencyNames;

    @Override
    public void init() {
        initConcurrentSets();

    }

    private void initConcurrentSets() {
        concurrentUserHashes = Sets.newConcurrentHashSet();
        concurrentCurrencySymbols = Sets.newConcurrentHashSet();
        concurrentCurrencyNames = Sets.newConcurrentHashSet();
    }

    private synchronized <T> boolean checkUniquenessAndAdd(Set<T> concurrentSet, T o){
        boolean isObjectContained = concurrentSet.contains(o);
        if(!isObjectContained){
            concurrentSet.add(o);
            return true;
        }
        return false;
    }

    private void updateCurrencyDataIndexes(CurrencyData currencyData) {
        currencyNameIndexes.put(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
        currencySymbolIndexes.put(new CurrencySymbolIndexData(currencyData.getSymbol(), currencyData.getHash()));
    }

    @Override
    public void updateCurrencies() {
        try {
            CurrencyData nativeCurrencyData = getNativeCurrency();
            if (nativeCurrencyData == null) {
                String recoveryServerAddress = networkService.getRecoveryServerAddress();
                nativeCurrencyData = restTemplate.getForObject(recoveryServerAddress + GET_NATIVE_CURRENCY_ENDPOINT, CurrencyData.class);
                if (nativeCurrencyData == null) {
                    throw new CurrencyException("Native currency recovery failed. Recovery sent null native currency");
                } else {
                    putCurrencyData(nativeCurrencyData);
                    setNativeCurrencyData(nativeCurrencyData);
                }
            }
        } catch (CurrencyException e) {
            throw e;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new CurrencyException(String.format("Native currency recovery failed. %s: %s", e.getClass().getName(), new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()));
        } catch (Exception e) {
            throw new CurrencyException(String.format("Native currency recovery failed. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    @Override
    public void putCurrencyData(CurrencyData currencyData) {
        super.putCurrencyData(currencyData);
        updateCurrencyDataIndexes(currencyData);
    }

//    @Override
    public ResponseEntity<IResponse> getUserTokenGenerationData(GetTokenGenerationDataRequest getTokenGenerationDataRequest) {
        //TODO 9/17/2019 astolia: maybe signature validation isn't required
        if(!getTokenGenerationDataRequestCrypto.verifySignature(getTokenGenerationDataRequest)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_GENERATION_INVALID_REQUEST, STATUS_ERROR));
        }
        UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(getTokenGenerationDataRequest.getSenderHash());
        GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
        userTokenGenerationData.getTransactionHashToCurrencyHashMap().entrySet().forEach( entry -> {
            Hash transactionHash = entry.getKey();
            if(transactionHash == null){
                getTokenGenerationDataResponse.
            }
            CurrencyData currencyData = pendingCurrencies.getByHash(transactionHash);
            if(currencyData != null)
        });
//        getTokenGenerationDataResponse.setTransactionHashToGeneratedCurrency(
//                userTokenGenerationData.getTransactionHashToCurrencyHashMap().entrySet().stream()
//                        .collect(Collectors.toMap(Map.Entry::getKey,
//                                entry -> currencies.getByHash(entry.getValue())
//                        )));
//        return ResponseEntity.ok(getTokenGenerationDataResponse);
    }

    public ResponseEntity<IResponse> generateToken(GenerateTokenRequest generateTokenRequest) {
        return null;
    }
}