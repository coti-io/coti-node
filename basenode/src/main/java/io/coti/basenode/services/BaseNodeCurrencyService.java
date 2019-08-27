package io.coti.basenode.services;

import io.coti.basenode.crypto.*;
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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class BaseNodeCurrencyService {

    private static final String RECOVERY_NODE_GET_CURRENCIES_ENDPOINT = "/currencies";
    private static final String RECOVERY_NODE_GET_CURRENCIES_UPDATE_ENDPOINT = "/currencies/update";
    private static final String RECOVERY_NODE_GET_NATIVE_CURRENCY_ENDPOINT = "/currencies/native";
    private static final int NUMBER_OF_NATIVE_CURRENCY = 1;

    private HashSet<Hash> missingCurrencyDataHashes;
    private EnumMap<CurrencyType, HashSet<Hash>> currencyHashByTypeMap;

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
    @Autowired
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @Autowired
    private GetUpdatedCurrencyResponseCrypto getUpdatedCurrencyResponseCrypto;
    @Autowired
    private CurrencyTypeCrypto currencyTypeCrypto;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
        missingCurrencyDataHashes = new HashSet<>();
        currencyHashByTypeMap = new EnumMap<>(CurrencyType.class);
        //TODO 8/22/2019 tomer: for initial testing
        if (networkService.getRecoveryServerAddress() == null) {
            initTestNativeCurrencyEntry();
        }
        updateExistingCurrencyHashByType();
        updateCurrencyDataByType();
//        recoverIfAbsentNativeCurrency();
    }

    public void updateExistingCurrencyHashByType() {
        currencies.forEach(currencyData -> {
            updateCurrencyHashByType(currencyData);
        });
    }

    protected void updateCurrencyHashByType(CurrencyData currencyData) {
        CurrencyType currencyType = currencyData.getCurrencyTypeData().getCurrencyType();
        currencyHashByTypeMap.computeIfAbsent(currencyType, key -> new HashSet<>());
        currencyHashByTypeMap.get(currencyType).add(currencyData.getHash());
    }

    private void updateCurrencyDataByType() {
        if (networkService.getRecoveryServerAddress() != null) {
            GetUpdatedCurrencyRequest getUpdatedCurrencyRequest = new GetUpdatedCurrencyRequest();
            getUpdatedCurrencyRequest.setCurrencyHashesByType(currencyHashByTypeMap);
            getUpdatedCurrencyRequestCrypto.signMessage(getUpdatedCurrencyRequest);

            ResponseEntity<GetUpdatedCurrencyResponse> getUpdatedCurrencyResponseResponseEntity =
                    getUpdatedCurrencyDataByTypeFromRecoveryServer(getUpdatedCurrencyRequest);
            HttpStatus statusCode = getUpdatedCurrencyResponseResponseEntity.getStatusCode();

            if (!statusCode.equals(HttpStatus.OK)) {
                log.error("Failed to retrieve updated currency data details");
            } else {
                GetUpdatedCurrencyResponse getUpdatedCurrencyResponse = getUpdatedCurrencyResponseResponseEntity.getBody();
                if (getUpdatedCurrencyResponseCrypto.verifySignature(getUpdatedCurrencyResponse)) {
                    Map<CurrencyType, HashSet<CurrencyData>> currencyHashesByType = getUpdatedCurrencyResponse.getCurrencyDataByType();
                    updateExistingCurrencyAccordingToRecovery(currencyHashesByType);
                } else {
                    log.error("Authorization error at retrieving updated currency data from recovery server");
                }
            }
        }
        verifyValidNativeCurrencyPresent();
    }

    private void verifyValidNativeCurrencyPresent() {
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(CurrencyType.NATIVE_COIN);
        if (nativeCurrencyHashes == null || nativeCurrencyHashes.isEmpty() || nativeCurrencyHashes.size() != NUMBER_OF_NATIVE_CURRENCY) {
            log.error("Failed to retrieve native currency data");
            System.exit(SpringApplication.exit(applicationContext));
        } else {
            CurrencyData nativeCurrencyData = currencies.getByHash(nativeCurrencyHashes.iterator().next());
            if (!currencyCrypto.verifySignature(nativeCurrencyData)) {
                log.error("Failed to verify native currency data of {}", nativeCurrencyData.getHash());
                System.exit(SpringApplication.exit(applicationContext));
            } else {
                if (!currencyTypeCrypto.verifySignature(nativeCurrencyData.getCurrencyTypeData())) {
                    log.error("Failed to verify native currency data type of {}", nativeCurrencyData.getHash());
                    System.exit(SpringApplication.exit(applicationContext));
                }
            }
        }
    }

    public CurrencyData getNativeCurrencyData() {
        CurrencyData nativeCurrencyData = null;
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(CurrencyType.NATIVE_COIN);
        if (nativeCurrencyHashes != null && !nativeCurrencyHashes.isEmpty()) {
            nativeCurrencyData = currencies.getByHash(nativeCurrencyHashes.iterator().next());
        }
        return nativeCurrencyData;
    }

    private void updateExistingCurrencyAccordingToRecovery(Map<CurrencyType, HashSet<CurrencyData>> currencyDataByTypeFromRecovery) {
        currencyDataByTypeFromRecovery.forEach((recoveredCurrencyType, recoveredCurrencyDataSet) -> {
            currencyHashByTypeMap.computeIfAbsent(recoveredCurrencyType, key -> new HashSet<>());
            recoveredCurrencyDataSet.forEach(recoveredCurrencyData -> {
                if (currencyCrypto.verifySignature(recoveredCurrencyData)) {
                    CurrencyData originalCurrencyData = currencies.getByHash(recoveredCurrencyData.getHash());
                    if (originalCurrencyData == null) {
                        if (currencyTypeCrypto.verifySignature(recoveredCurrencyData.getCurrencyTypeData())) {
                            putCurrencyData(recoveredCurrencyData); // For an entirely new currency data
                        } else {
                            log.error("Failed to authenticate recovered updated currency data type {}", recoveredCurrencyData.getCurrencyTypeData());
                        }
                    } else {
                        CurrencyTypeData recoveredCurrencyTypeData = recoveredCurrencyData.getCurrencyTypeData();
                        if (currencyTypeCrypto.verifySignature(recoveredCurrencyTypeData)) {
                            if (!originalCurrencyData.getCurrencyTypeData().getCurrencyType().equals(recoveredCurrencyTypeData.getCurrencyType())) {
                                updateTypeForExistingCurrencyData(originalCurrencyData, recoveredCurrencyData);
                            } else {
                                log.error("Expected either new Currency Data {} or different Currency type {}", recoveredCurrencyData.getHash(), recoveredCurrencyType);
                            }

                        } else {
                            log.error("Failed to authenticate recovered updated currency data type {}", recoveredCurrencyTypeData);
                        }
                    }
                } else {
                    log.error("Signature verification error for recovered currency data {}", recoveredCurrencyData.getHash());
                }
            });
        });
    }

    private void updateTypeForExistingCurrencyData(CurrencyData originalCurrencyData, CurrencyData recoveredCurrencyData) {
        CurrencyType originalCurrencyType = originalCurrencyData.getCurrencyTypeData().getCurrencyType();
        currencies.delete(originalCurrencyData);
        currencies.put(recoveredCurrencyData);
        currencyHashByTypeMap.get(originalCurrencyType).remove(originalCurrencyData.getHash());
        updateCurrencyHashByType(recoveredCurrencyData);
    }

    private ResponseEntity<GetUpdatedCurrencyResponse> getUpdatedCurrencyDataByTypeFromRecoveryServer(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        HttpEntity<GetUpdatedCurrencyRequest> entity = new HttpEntity<>(getUpdatedCurrencyRequest);
        return restTemplate.postForEntity(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_CURRENCIES_UPDATE_ENDPOINT,
                entity, GetUpdatedCurrencyResponse.class);
    }

    public ResponseEntity<BaseResponse> getUpdatedCurrencies(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        if (!getUpdatedCurrencyRequestCrypto.verifySignature(getUpdatedCurrencyRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType = getUpdatedCurrencyRequest.getCurrencyHashesByType();
        Map<CurrencyType, HashSet<CurrencyData>> updatedCurrencyDataByHash = new EnumMap<CurrencyType, HashSet<CurrencyData>>(CurrencyType.class);

        updateIfNeededCurrencyDataByType(existingCurrencyHashesByType, updatedCurrencyDataByHash);

        GetUpdatedCurrencyResponse getUpdatedCurrencyResponse = new GetUpdatedCurrencyResponse();
        getUpdatedCurrencyResponse.setCurrencyDataByType(updatedCurrencyDataByHash);
        getUpdatedCurrencyResponseCrypto.signMessage(getUpdatedCurrencyResponse);

        return ResponseEntity.status(HttpStatus.OK).body(getUpdatedCurrencyResponse);
    }

    private void updateIfNeededCurrencyDataByType(Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType, Map<CurrencyType, HashSet<CurrencyData>> updatedCurrencyDataByHash) {
        currencyHashByTypeMap.forEach((localCurrencyType, localCurrencyHashes) -> {
            HashSet<Hash> tempExistingCurrencyHashesByType = existingCurrencyHashesByType.get(localCurrencyType);

            if (tempExistingCurrencyHashesByType != null && !tempExistingCurrencyHashesByType.isEmpty()) {
                localCurrencyHashes.forEach(localCurrencyHash -> {
                    if (!tempExistingCurrencyHashesByType.contains(localCurrencyHash)) {
                        updatedCurrencyDataByHash.computeIfAbsent(localCurrencyType, key -> new HashSet<>());
                        addToUpdatedCurrencyMap(updatedCurrencyDataByHash, localCurrencyType, localCurrencyHash);
                    }
                });
            } else {
                updatedCurrencyDataByHash.computeIfAbsent(localCurrencyType, key -> new HashSet<>());
                localCurrencyHashes.forEach(localCurrencyHash -> {
                    addToUpdatedCurrencyMap(updatedCurrencyDataByHash, localCurrencyType, localCurrencyHash);
                });
            }
        });
    }

    private void addToUpdatedCurrencyMap(Map<CurrencyType, HashSet<CurrencyData>> updatedCurrencyDataByHash, CurrencyType localCurrencyType, Hash localCurrencyHash) {
        CurrencyData updatedCurrencyData = currencies.getByHash(localCurrencyHash);
        if (updatedCurrencyData != null) {
            updatedCurrencyDataByHash.get(localCurrencyType).add(updatedCurrencyData);
        } else {
            log.error("Failed to retrieve CurrencyData {}", localCurrencyHash);
        }
    }

    public void recoverIfAbsentNativeCurrency() {
        CurrencyData nativeCurrency = null;
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(CurrencyType.NATIVE_COIN);
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
        return nativeCurrency != null && currencyCrypto.verifySignature(nativeCurrency) &&
                nativeCurrency.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN);
    }

    private ResponseEntity<BaseResponse> getNativeCurrencyFromRecoveryServer() {
        ResponseEntity<GetNativeCurrencyResponse> responseEntity = restTemplate.getForEntity(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_NATIVE_CURRENCY_ENDPOINT,
                GetNativeCurrencyResponse.class);
        return ResponseEntity.status(responseEntity.getStatusCode()).body(responseEntity.getBody());
    }

    public ResponseEntity<BaseResponse> getMissingNativeCurrency() {
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(CurrencyType.NATIVE_COIN);
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

    public Set<Hash> getMissingCurrencyDataHashes() {
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

    //TODO 8/25/2019 tomer: No longer needed
    public void requestMissingCurrenciesFromRecoveryServer() {
        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
        getCurrencyRequest.setCurrenciesHashes(missingCurrencyDataHashes);
        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);

        ResponseEntity<GetCurrencyResponse> getCurrenciesResponseEntity = getCurrenciesFromRecoveryServer(getCurrencyRequest);

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

    private ResponseEntity<GetCurrencyResponse> getCurrenciesFromRecoveryServer(GetCurrencyRequest getCurrencyRequest) {
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
        CurrencyType currencyType = CurrencyType.PAYMENT_CMD_TOKEN;
        if (currencyHashByTypeMap.get(CurrencyType.NATIVE_COIN) == null ||
                currencyHashByTypeMap.get(CurrencyType.NATIVE_COIN).isEmpty()) {
            currencyType = CurrencyType.NATIVE_COIN;
        }
        CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now(), null);
        currencyTypeCrypto.signMessage(currencyTypeData);
        currencyData.setCurrencyTypeData(currencyTypeData);
        addCurrencyHashByType(currencyData, currencyTypeData);
        currencyCrypto.signMessage(currencyData);
        return currencyData;
    }

    private void addCurrencyHashByType(CurrencyData currencyData, CurrencyTypeData currencyTypeData) {
        //TODO 8/22/2019 tomer: for initial testing
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(currencyTypeData);
        if (nativeCurrencyHashes == null) {
            HashSet<Hash> currencyHashes = new HashSet<>();
            currencyHashByTypeMap.put(currencyTypeData.getCurrencyType(), currencyHashes);
        }
        currencyHashByTypeMap.get(currencyTypeData.getCurrencyType()).add(currencyData.getHash());
    }


    public void verifyCurrencyExists(Hash currencyDataHash) {
        if (currencies.getByHash(currencyDataHash) == null) {
            log.error("Failed to locate Currency {}", currencyDataHash);
            System.exit(SpringApplication.exit(applicationContext));
        }
    }
}
