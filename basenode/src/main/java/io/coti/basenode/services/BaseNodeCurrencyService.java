package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.CurrencyCrypto;
import io.coti.basenode.crypto.GetCurrencyRequestCrypto;
import io.coti.basenode.crypto.GetCurrencyResponseCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.*;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.model.CurrencySymbolIndexes;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class BaseNodeCurrencyService {

    private static final String RECOVERY_NODE_GET_CURRENCIES_ENDPOINT = "/currencies";
    private static final String RECOVERY_NODE_GET_NATIVE_CURRENCY_ENDPOINT = "/currencies/native";
    private static final int NUMBER_OF_NATIVE_CURRENCY = 1;

    private HashSet<Hash> missingCurrencyDataHashes;
    private HashMap<CurrencyTempType, HashSet<Hash>> currencyHashByTypeMap;

    @Autowired
    private Currencies currencies;
    @Autowired
    private GetCurrencyRequestCrypto getCurrencyRequestCrypto;
    @Autowired
    private GetCurrencyResponseCrypto getCurrencyResponseCrypto;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CurrencyCrypto currencyCrypto;
    @Autowired
    private CurrencyNameIndexes currencyNameIndexes;
    @Autowired
    private CurrencySymbolIndexes currencySymbolIndexes;
    @Autowired
    protected ApplicationContext applicationContext;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
        missingCurrencyDataHashes = new HashSet<>();
        currencyHashByTypeMap = new HashMap<>();
        //TODO 8/22/2019 tomer: for initial testing
        if (networkService.getRecoveryServerAddress() == null) {
            initTestNativeCurrencyEntry();
        }

        updateExistingCurrencyHashByType();
        recoverIfAbsentNativeCurrency();
    }

    public void updateExistingCurrencyHashByType() {
        currencies.forEach(currencyData -> {
            updateCurrencyHashByType(currencyData);
        });
    }

    protected void updateCurrencyHashByType(CurrencyData currencyData) {
        CurrencyTempType currencyType = currencyData.getType();
        if (currencyHashByTypeMap.get(currencyType) == null) {
            currencyHashByTypeMap.put(currencyType, new HashSet<>());
        }
        currencyHashByTypeMap.get(currencyType).add(currencyData.getHash());
    }

    public void recoverIfAbsentNativeCurrency() {
        CurrencyData nativeCurrency = null;
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(CurrencyTempType.Native);
        if (nativeCurrencyHashes == null || nativeCurrencyHashes.isEmpty()) {
            recoverNativeCurrency();
        } else {
            if (nativeCurrencyHashes.size() > NUMBER_OF_NATIVE_CURRENCY) {
                log.error("More than one native coin has been identified {}", nativeCurrencyHashes.toString());
                System.exit(SpringApplication.exit(applicationContext));
            } else {
                nativeCurrency = currencies.getByHash(nativeCurrencyHashes.iterator().next());
                if (!verifyNativeCurrency(nativeCurrency)) {
                    log.error("Existing Native coin {} is invalid", nativeCurrency.getName());
                    System.exit(SpringApplication.exit(applicationContext));
                }
            }
        }
    }

    protected void recoverNativeCurrency() {
        CurrencyData nativeCurrency;
        if (networkService.getRecoveryServerAddress() != null) {
            ResponseEntity<BaseResponse> recoverNativeCurrency = getNativeCurrencyFromRecoveryServer();
            if (recoverNativeCurrency.getStatusCode().equals(HttpStatus.OK)) {
                nativeCurrency = ((GetNativeCurrencyResponse) recoverNativeCurrency.getBody()).getNativeCurrency();
                if (verifyNativeCurrency(nativeCurrency)) {
                    putCurrencyData(nativeCurrency);
                } else {
                    log.error("No valid native coin has been identified or was recovered");
                    System.exit(SpringApplication.exit(applicationContext));
                }
            } else {
                log.error("No native coin has been identified or was recovered");
                System.exit(SpringApplication.exit(applicationContext));
            }
        } else {
            log.error("Native coin could not be recovered");
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    protected boolean verifyNativeCurrency(CurrencyData nativeCurrency) {
        return nativeCurrency != null && currencyCrypto.verifySignature(nativeCurrency) && nativeCurrency.getType().equals(CurrencyTempType.Native);
    }

    private ResponseEntity<BaseResponse> getNativeCurrencyFromRecoveryServer() {
        ResponseEntity<GetNativeCurrencyResponse> responseEntity = restTemplate.getForEntity(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_NATIVE_CURRENCY_ENDPOINT,
                GetNativeCurrencyResponse.class);
        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    }

    public ResponseEntity<BaseResponse> getMissingNativeCurrency() {
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(CurrencyTempType.Native);
        if (nativeCurrencyHashes == null || nativeCurrencyHashes.isEmpty() || nativeCurrencyHashes.size() != NUMBER_OF_NATIVE_CURRENCY) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(NATIVE_COIN_NOT_FOUND, STATUS_ERROR));
        }
        Hash nativeCurrencyHash = nativeCurrencyHashes.iterator().next();
        CurrencyData nativeCurrencyData = currencies.getByHash(nativeCurrencyHash);
        if (nativeCurrencyData != null && currencyCrypto.verifySignature(nativeCurrencyData)) {
            return ResponseEntity.status(HttpStatus.OK).body(new GetNativeCurrencyResponse(nativeCurrencyData));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response(NO_VALID_NATIVE_COIN_NOT_FOUND, STATUS_ERROR));
    }

    public ResponseEntity<BaseResponse> getMissingCurrencies(GetCurrencyRequest getCurrencyRequest) {
        if (!getCurrencyRequestCrypto.verifySignature(getCurrencyRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        Set<Hash> missingCurrenciesHashes = getCurrencyRequest.getCurrenciesHashes();
        GetCurrencyResponse getCurrencyResponse = new GetCurrencyResponse();
        Set<CurrencyData> retrievedCurrencyDataSet = new HashSet<>();

        if (!missingCurrenciesHashes.isEmpty()) {
            missingCurrenciesHashes.forEach(missingCurrenciesHash -> {
                CurrencyData retrievedCurrencyData = currencies.getByHash(missingCurrenciesHash);
                if (retrievedCurrencyData == null) {
                    log.error("Failed to retrieve currency data with hash {}", missingCurrenciesHash);
                } else {
                    retrievedCurrencyDataSet.add(retrievedCurrencyData);
                }
            });
        }

        getCurrencyResponse.setCurrencyDataSet(retrievedCurrencyDataSet);
        getCurrencyResponseCrypto.signMessage(getCurrencyResponse);
        return ResponseEntity.status(HttpStatus.OK).body(getCurrencyResponse);
    }

    public HashSet<Hash> getMissingCurrencyDataHashes() {
        return missingCurrencyDataHashes;
    }

    public void updateMissingCurrencyDataHashesFromClusterStamp(Hash currencyDataHash) {
        if (currencyDataHash != null && currencies.getByHash(currencyDataHash) == null) {
            missingCurrencyDataHashes.add(currencyDataHash);
        }
    }

    public void removeHashFromMissingCurrencyDataHashes(Hash currencyDataHash) {
        if (currencyDataHash != null) {
            boolean removedMissingCurrencyDataHash = missingCurrencyDataHashes.remove(currencyDataHash);
            if (!removedMissingCurrencyDataHash) {
                log.info("Failed to remove missing currency hash {}", currencyDataHash);
            }
        }
    }

    public void requestMissingCurrenciesFromRecoveryServer() {
        Set<Hash> missingCurrencyDataHashes = getMissingCurrencyDataHashes();
        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
        getCurrencyRequest.setCurrenciesHashes(missingCurrencyDataHashes);
        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);

        ResponseEntity<GetCurrencyResponse> getCurrenciesResponseEntity = getGetCurrenciesFromRecoveryServer(getCurrencyRequest);

        HttpStatus statusCode = getCurrenciesResponseEntity.getStatusCode();
        if (!statusCode.equals(HttpStatus.OK)) {
            log.error("Error at getting missing currencies from recovery server");
        } else {
            GetCurrencyResponse getCurrencyResponse = getCurrenciesResponseEntity.getBody();
            if (getCurrencyResponseCrypto.verifySignature(getCurrencyResponse)) {
                getCurrencyResponse.getCurrencyDataSet().forEach(currencyData -> {
                    if (currencyCrypto.verifySignature(currencyData)) {
                        putCurrencyData(currencyData);
                        removeHashFromMissingCurrencyDataHashes(currencyData.getHash());
                    } else {
                        log.error("Authorization error for missing currency {}", currencyData.getHash());
                    }
                });
            } else {
                log.error("Authorization error at getting missing currencies from recovery server");
            }
        }
    }

    protected void putCurrencyData(CurrencyData currencyData) {
        currencies.put(currencyData);
        currencyNameIndexes.put(new CurrencyNameIndexData(CryptoHelper.cryptoHash(currencyData.getName().getBytes()), currencyData.getHash()));
        currencySymbolIndexes.put(new CurrencySymbolIndexData(CryptoHelper.cryptoHash(currencyData.getSymbol().getBytes()), currencyData.getHash()));
        updateCurrencyHashByType(currencyData);
    }

    private ResponseEntity<GetCurrencyResponse> getGetCurrenciesFromRecoveryServer(GetCurrencyRequest getCurrencyRequest) {
        HttpEntity<GetCurrencyRequest> entity = new HttpEntity<>(getCurrencyRequest);
        return restTemplate.postForEntity(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_CURRENCIES_ENDPOINT,
                entity, GetCurrencyResponse.class);
    }

    public void initTestNativeCurrencyEntry() {
        //TODO 8/22/2019 tomer: used for initial testing, creating initial Native currency
        Hash nativeCurrencyHash = new Hash("723dc3f71c4033ebabcec23beb56eeb3d29f3e58a8fdb0958ee16ec28cde4606f4e94b1e2507af4cd852ced100a8465ae142cf9e1981d86021fb32a0d0e213bf7aafa4d4");
        CurrencyData currencyData = createCurrencyData("Coti Native Coin", "Coti Native", nativeCurrencyHash);
        putCurrencyData(currencyData);
    }

    public void initTestCurrencyDataEntries() {
        //TODO 8/14/2019 tomer: used for initial testing, remove to dedicated test files
        Set<Hash> missingCurrencyDataHashes = getMissingCurrencyDataHashes();
        missingCurrencyDataHashes.forEach(missingCurrencyDataHash -> {
            String substring = missingCurrencyDataHash.toString().substring(0, 6);
            CurrencyData currencyData = createCurrencyData("Coti Coin " + substring, "Coti " + substring, missingCurrencyDataHash);
            putCurrencyData(currencyData);
        });
    }

    private CurrencyData createCurrencyData(String name, String symbol, Hash hash) {
        //TODO 8/22/2019 tomer: for initial testing
        CurrencyData currencyData = new CurrencyData();
        currencyData.setName(name);
        currencyData.setSymbol(symbol);
        currencyData.setHash(hash);
        currencyData.setTotalSupply(new BigDecimal(700000));
        currencyData.setScale(8);
        currencyData.setCreationTime(Instant.now());
        currencyData.setDescription("tempDescription");
        currencyData.setRegistrarHash(new Hash("tempRegistrar"));
        currencyData.setSignerHash(new Hash("tempSigner"));
        currencyData.setOriginatorHash(new Hash("tempOriginator"));
        CurrencyTempType currencyTempType = CurrencyTempType.Payment;
        if (currencyHashByTypeMap.get(CurrencyTempType.Native) == null || currencyHashByTypeMap.get(CurrencyTempType.Native).isEmpty()) {
            currencyTempType = CurrencyTempType.Native;
        }
        addCurrencyHashByType(currencyData, currencyTempType);
        currencyData.setType(currencyTempType);
        currencyCrypto.signMessage(currencyData);
        return currencyData;
    }

    private void addCurrencyHashByType(CurrencyData currencyData, CurrencyTempType currencyTempType) {
        //TODO 8/22/2019 tomer: for initial testing
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(currencyTempType);
        if (nativeCurrencyHashes == null) {
            HashSet<Hash> currencyHashes = new HashSet<>();
            currencyHashByTypeMap.put(currencyTempType, currencyHashes);
        }
        currencyHashByTypeMap.get(currencyTempType).add(currencyData.getHash());
    }

}
