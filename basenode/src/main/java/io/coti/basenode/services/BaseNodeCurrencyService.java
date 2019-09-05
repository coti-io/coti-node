package io.coti.basenode.services;

import io.coti.basenode.crypto.CurrencyCrypto;
import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.GetUpdatedCurrencyRequestCrypto;
import io.coti.basenode.crypto.GetUpdatedCurrencyResponseCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.*;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class BaseNodeCurrencyService {
    private static final String RECOVERY_NODE_GET_CURRENCIES_UPDATE_ENDPOINT = "/currencies/update";
    private static final String RECOVERY_NODE_GET_CURRENCIES_UPDATE_REACTIVE_ENDPOINT = "/currencies/update/reactive";
    private static final int NUMBER_OF_NATIVE_CURRENCY = 1;

    private EnumMap<CurrencyType, HashSet<Hash>> currencyHashByTypeMap;

    @Autowired
    private Currencies currencies;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CurrencyCrypto currencyCrypto;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @Autowired
    private GetUpdatedCurrencyResponseCrypto getUpdatedCurrencyResponseCrypto;
    @Autowired
    private CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @Autowired
    private HttpJacksonSerializer jacksonSerializer;
    @Autowired
    private BaseNodeCurrencyChunkService baseNodeCurrencyChunkService;


    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
        currencyHashByTypeMap = new EnumMap<>(CurrencyType.class);
        //TODO 8/22/2019 tomer: for initial testing
        if (networkService.getRecoveryServerAddress() == null) {
            initTestNativeCurrencyEntry();
            initTestNonNativeCurrencyEntries();
        }
        updateCurrencyHashByTypeMapFromExistingCurrencies();
//        updateCurrencyDataByTypeFromRecoveryServer();
        updateCurrencyDataByTypeFromRecoveryServerReactive();
    }

    public void updateCurrencyHashByTypeMapFromExistingCurrencies() {
        currencies.forEach(currencyData -> {
            updateCurrencyHashByTypeMapOfCurrency(currencyData);
        });
    }

    protected void updateCurrencyHashByTypeMapOfCurrency(CurrencyData currencyData) {
        CurrencyType currencyType = currencyData.getCurrencyTypeData().getCurrencyType();
        currencyHashByTypeMap.computeIfAbsent(currencyType, key -> new HashSet<>());
        currencyHashByTypeMap.get(currencyType).add(currencyData.getHash());
    }

    protected HashSet getCurrencyHashesByCurrencyType(CurrencyType currencyType) {
        return currencyHashByTypeMap.get(currencyType);
    }

    private void updateCurrencyDataByTypeFromRecoveryServerReactive() {
        if (networkService.getRecoveryServerAddress() != null) {
            GetUpdatedCurrencyRequest getUpdatedCurrencyRequest = new GetUpdatedCurrencyRequest();
            getUpdatedCurrencyRequest.setCurrencyHashesByType(currencyHashByTypeMap);
            getUpdatedCurrencyRequestCrypto.signMessage(getUpdatedCurrencyRequest);


            OutputStream outputStream = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    //TODO 9/5/2019 tomer:
                    log.info("Implement here {}", b);
                }
            };
            PrintWriter output2 = new PrintWriter(outputStream);
//                PrintWriter output = response.getWriter();

            getUpdatedCurrencyDataByTypeReactiveFromRecoveryServer(getUpdatedCurrencyRequest, output2);

//            output2.

            //TODO 9/4/2019 tomer: process output
            int iPause = 7;

            //            HttpStatus statusCode = getUpdatedCurrencyResponseResponseEntity.getStatusCode();

        }
        verifyValidNativeCurrencyPresent();

    }

    private void getUpdatedCurrencyDataByTypeReactiveFromRecoveryServer(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest, PrintWriter output) {
        RestTemplate restTemplate = new RestTemplate();
        CustomRequestCallBack requestCallBack = new CustomRequestCallBack(jacksonSerializer, getUpdatedCurrencyRequest);
        baseNodeCurrencyChunkService.currencyHandler(responseExtractor ->
                        restTemplate.execute(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_CURRENCIES_UPDATE_REACTIVE_ENDPOINT,
                                HttpMethod.POST, requestCallBack, responseExtractor)
                , output);
    }

    private void updateCurrencyDataByTypeFromRecoveryServer() {
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

    private ResponseEntity<GetUpdatedCurrencyResponse> getUpdatedCurrencyDataByTypeFromRecoveryServer(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        HttpEntity<GetUpdatedCurrencyRequest> entity = new HttpEntity<>(getUpdatedCurrencyRequest);
        return restTemplate.postForEntity(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_CURRENCIES_UPDATE_ENDPOINT,
                entity, GetUpdatedCurrencyResponse.class);
    }

    private void updateExistingCurrencyAccordingToRecovery(Map<CurrencyType, HashSet<CurrencyData>> currencyDataByTypeFromRecovery) {
        currencyDataByTypeFromRecovery.forEach((recoveredCurrencyType, recoveredCurrencyDataSet) -> {
            currencyHashByTypeMap.computeIfAbsent(recoveredCurrencyType, key -> new HashSet<>());
            recoveredCurrencyDataSet.forEach(recoveredCurrencyData -> {
                if (currencyCrypto.verifySignature(recoveredCurrencyData)) {
                    CurrencyData originalCurrencyData = currencies.getByHash(recoveredCurrencyData.getHash());
                    if (originalCurrencyData == null) {
                        CurrencyTypeRegistrationData recoveredCurrencyTypeRegistrationData = new CurrencyTypeRegistrationData(recoveredCurrencyData);
                        if (currencyTypeRegistrationCrypto.verifySignature(recoveredCurrencyTypeRegistrationData)) {
                            putCurrencyData(recoveredCurrencyData); // For a new currency data
                        } else {
                            log.error("Failed to authenticate recovered updated currency data type {}", recoveredCurrencyTypeRegistrationData.getCurrencyType().getText());
                        }
                    } else {
                        CurrencyTypeRegistrationData recoveredCurrencyTypeRegistrationData = new CurrencyTypeRegistrationData(recoveredCurrencyData);
                        if (currencyTypeRegistrationCrypto.verifySignature(recoveredCurrencyTypeRegistrationData)) {
                            if (!originalCurrencyData.getCurrencyTypeData().getCurrencyType().equals(recoveredCurrencyTypeRegistrationData.getCurrencyType())) {
                                replaceExistingCurrencyDataDueToTypeChange(originalCurrencyData, recoveredCurrencyData);
                            } else {
                                log.error("Expected either new Currency Data {} or different Currency type {}", recoveredCurrencyData.getHash(), recoveredCurrencyType);
                            }
                        } else {
                            log.error("Failed to authenticate recovered updated currency data type {}", recoveredCurrencyTypeRegistrationData.getCurrencyType());
                        }
                    }
                } else {
                    log.error("Signature verification error for recovered currency data {}", recoveredCurrencyData.getHash());
                }
            });
        });
    }

    protected void replaceExistingCurrencyDataDueToTypeChange(CurrencyData originalCurrencyData, CurrencyData recoveredCurrencyData) {
        CurrencyType originalCurrencyType = originalCurrencyData.getCurrencyTypeData().getCurrencyType();
        currencies.delete(originalCurrencyData);
        currencies.put(recoveredCurrencyData);
        currencyHashByTypeMap.get(originalCurrencyType).remove(originalCurrencyData.getHash());
        updateCurrencyHashByTypeMapOfCurrency(recoveredCurrencyData);
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
                CurrencyTypeRegistrationData nativeCurrencyTypeRegistrationData = new CurrencyTypeRegistrationData(nativeCurrencyData);
                if (!currencyTypeRegistrationCrypto.verifySignature(nativeCurrencyTypeRegistrationData)) {
                    log.error("Failed to verify native currency data type of {}", nativeCurrencyTypeRegistrationData.getCurrencyType().getText());
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

    public void getUpdatedCurrenciesReactive(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest, FluxSink<CurrencyData> fluxSink) {
        if (!getUpdatedCurrencyRequestCrypto.verifySignature(getUpdatedCurrencyRequest)) {
            log.error("Authorization check failed on request to get updated currencies.");
        }
        Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType = getUpdatedCurrencyRequest.getCurrencyHashesByType();
        getRequiringUpdateOfCurrencyDataByTypeReactive(existingCurrencyHashesByType, fluxSink);

    }

    private void getRequiringUpdateOfCurrencyDataByTypeReactive(Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType, FluxSink<CurrencyData> fluxSink) {
        currencyHashByTypeMap.forEach((localCurrencyType, localCurrencyHashes) -> {
            HashSet<Hash> tempExistingCurrencyHashesByType = existingCurrencyHashesByType.get(localCurrencyType);

            if (tempExistingCurrencyHashesByType != null && !tempExistingCurrencyHashesByType.isEmpty()) {
                localCurrencyHashes.forEach(localCurrencyHash -> {
                    if (!tempExistingCurrencyHashesByType.contains(localCurrencyHash)) {
                        addToUpdatedCurrencyMapReactive(localCurrencyHash, fluxSink);
                    }
                });
            } else {
                localCurrencyHashes.forEach(localCurrencyHash -> {
                    addToUpdatedCurrencyMapReactive(localCurrencyHash, fluxSink);
                });
            }
        });
    }

    private void addToUpdatedCurrencyMapReactive(Hash localCurrencyHash, FluxSink<CurrencyData> fluxSink) {
        CurrencyData updatedCurrencyData = currencies.getByHash(localCurrencyHash);
        if (updatedCurrencyData != null) {
            fluxSink.next(updatedCurrencyData);
        } else {
            log.error("Failed to retrieve CurrencyData {}", localCurrencyHash);
        }
    }

    public ResponseEntity<BaseResponse> getUpdatedCurrencies(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        if (!getUpdatedCurrencyRequestCrypto.verifySignature(getUpdatedCurrencyRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType = getUpdatedCurrencyRequest.getCurrencyHashesByType();
        Map<CurrencyType, HashSet<CurrencyData>> updatedCurrencyDataByHash = getRequiringUpdateOfCurrencyDataByType(existingCurrencyHashesByType);

        GetUpdatedCurrencyResponse getUpdatedCurrencyResponse = new GetUpdatedCurrencyResponse();
        getUpdatedCurrencyResponse.setCurrencyDataByType(updatedCurrencyDataByHash);
        getUpdatedCurrencyResponseCrypto.signMessage(getUpdatedCurrencyResponse);

        return ResponseEntity.status(HttpStatus.OK).body(getUpdatedCurrencyResponse);
    }

    private Map<CurrencyType, HashSet<CurrencyData>> getRequiringUpdateOfCurrencyDataByType(Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType) {
        Map<CurrencyType, HashSet<CurrencyData>> updatedCurrencyDataByHash = new EnumMap<CurrencyType, HashSet<CurrencyData>>(CurrencyType.class);
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
        return updatedCurrencyDataByHash;
    }

    private void addToUpdatedCurrencyMap(Map<CurrencyType, HashSet<CurrencyData>> updatedCurrencyDataByHash, CurrencyType localCurrencyType,
                                         Hash localCurrencyHash) {
        CurrencyData updatedCurrencyData = currencies.getByHash(localCurrencyHash);
        if (updatedCurrencyData != null) {
            updatedCurrencyDataByHash.get(localCurrencyType).add(updatedCurrencyData);
        } else {
            log.error("Failed to retrieve CurrencyData {}", localCurrencyHash);
        }
    }

    protected void putCurrencyData(CurrencyData currencyData) {
        if (currencyData == null) {
            log.error("Failed to add an empty currency");
            return;
        }
        currencies.put(currencyData);
        updateCurrencyDataIndexes(currencyData);
        updateCurrencyHashByTypeMapOfCurrency(currencyData);
    }

    protected void updateCurrencyDataIndexes(CurrencyData currencyData) {
        // Implemented by Financial Server node
    }

    private void removeCurrencyData(CurrencyData currencyData) {
        if (currencyData == null) {
            log.error("Failed to remove an empty currency");
            return;
        }
        currencies.delete(currencyData);
        removeCurrencyDataIndexes(currencyData);
        currencyHashByTypeMap.get(currencyData.getCurrencyTypeData().getCurrencyType()).remove(currencyData.getHash());
    }

    protected void removeCurrencyDataIndexes(CurrencyData currencyData) {
        // Implemented by Financial Server node
    }

    public void verifyCurrencyExists(Hash currencyDataHash) {
        if (currencies.getByHash(currencyDataHash) == null) {
            log.error("Failed to locate Currency {}", currencyDataHash);
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    public void initTestNativeCurrencyEntry() {
        //TODO 8/22/2019 tomer: used for initial testing, creating initial Native currency
        Hash nativeCurrencyHash = new Hash("723dc3f71c4033ebabcec23beb56eeb3d29f3e58a8fdb0958ee16ec28cde4606f4e94b1e2507af4cd852ced100a8465ae142cf9e1981d86021fb32a0d0e213bf7aafa4d4");
        CurrencyData currencyData = createCurrencyData("Coti Native Coin", ("CotiNative").toUpperCase(), nativeCurrencyHash);
        putCurrencyData(currencyData);
    }

    public void initTestNonNativeCurrencyEntries() {
        //TODO 8/14/2019 tomer: used for initial testing, remove to dedicated test files
        HashSet<Hash> currencyHashes = new HashSet<>();
        currencyHashes.add(new Hash("5fcb5346d5ca9c7b3a21543119561c581b87d772ce1634b6a34ce841c37c9501fd68e7269aec7d7bbc448fcf57500da571726eec19959d844191f4c17d0b96b84427b767"));
        currencyHashes.add(new Hash("6fcb5346d5ca9c7b3a21543119561c581b87d772ce1634b6a34ce841c37c9501fd68e7269aec7d7bbc448fcf57500da571726eec19959d844191f4c17d0b96b84427b767"));

        currencyHashes.forEach(missingCurrencyDataHash -> {
            String substring = missingCurrencyDataHash.toString().substring(0, 6);
            CurrencyData currencyData = createCurrencyData("Coti Coin " + substring, ("Coti").toUpperCase(), missingCurrencyDataHash);
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
        currencyData.setCurrencyTypeData(currencyTypeData);
        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyData);
//                new CurrencyTypeRegistrationData(hash, currencyType, Instant.now());
        currencyTypeRegistrationCrypto.signMessage(currencyTypeRegistrationData);
//        currencyTypeCrypto.signMessage(currencyTypeData);
        currencyTypeData.setSignature(currencyTypeRegistrationData.getSignature());
//        currencyData.setCurrencyTypeData(currencyTypeData);
        currencyCrypto.signMessage(currencyData);
        return currencyData;
    }


}
