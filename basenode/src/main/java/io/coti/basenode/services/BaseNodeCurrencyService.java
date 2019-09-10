package io.coti.basenode.services;

import io.coti.basenode.crypto.CurrencyCrypto;
import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.GetUpdatedCurrencyRequestCrypto;
import io.coti.basenode.crypto.GetUpdatedCurrencyResponseCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CurrencyInitializationException;
import io.coti.basenode.http.*;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.FluxSink;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Pattern;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class BaseNodeCurrencyService implements ICurrencyService {
    public static final Hash NATIVE_CURRENCY_HASH = null;
    private static final String RECOVERY_NODE_GET_CURRENCIES_UPDATE_ENDPOINT = "/currencies/update";
    private static final String RECOVERY_NODE_GET_CURRENCIES_UPDATE_REACTIVE_ENDPOINT = "/currencies/update/reactive";
    private static final int NUMBER_OF_NATIVE_CURRENCY = 1;

    private EnumMap<CurrencyType, HashSet<Hash>> currencyHashByTypeMap;
    private CurrencyData nativeCurrencyData;

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
        nativeCurrencyData = null;
        updateCurrencyHashByTypeMapFromExistingCurrencies();
        updateCurrencies();
    }


    public void updateCurrencyHashByTypeMapFromExistingCurrencies() {
        currencies.forEach(currencyData -> {
            updateCurrencyHashByTypeMap(currencyData);
        });
        if (!currencies.isEmpty()) {
            verifyValidNativeCurrencyPresent();
        }
    }

    protected void updateCurrencyHashByTypeMap(CurrencyData currencyData) {
        CurrencyType currencyType = currencyData.getCurrencyTypeData().getCurrencyType();
        currencyHashByTypeMap.putIfAbsent(currencyType, new HashSet<>());
        currencyHashByTypeMap.get(currencyType).add(currencyData.getHash());
    }

    protected HashSet getCurrencyHashesByCurrencyType(CurrencyType currencyType) {
        return currencyHashByTypeMap.get(currencyType);
    }

    public void updateCurrencies() {
        //        updateCurrencyDataByTypeFromRecoveryServer();
        updateCurrencyDataByTypeFromRecoveryServerReactive();
    }

    private void updateCurrencyDataByTypeFromRecoveryServerReactive() {
        if (networkService.getRecoveryServerAddress() != null) {
            GetUpdatedCurrencyRequest getUpdatedCurrencyRequest = new GetUpdatedCurrencyRequest();
            getUpdatedCurrencyRequest.setCurrencyHashesByType(currencyHashByTypeMap);
            getUpdatedCurrencyRequestCrypto.signMessage(getUpdatedCurrencyRequest);


//            OutputStream outputStream = new OutputStream() {
//                @Override
//                public void write(int b) throws IOException {
//                    //TODO 9/5/2019 tomer:
//                    log.info("Implement here {}", b);
//                }
//            };

            ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream1);

            log.info("{} Before calling recovery server", this.getClass().getSimpleName());
            getUpdatedCurrencyDataByTypeReactiveFromRecoveryServer(getUpdatedCurrencyRequest, printWriter);
            log.info("{} After calling recovery server", this.getClass().getSimpleName());

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
        updateCurrencyHashByTypeMap(recoveredCurrencyData);
    }

    private void verifyValidNativeCurrencyPresent() {
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(CurrencyType.NATIVE_COIN);
        if (nativeCurrencyHashes == null || nativeCurrencyHashes.isEmpty() || nativeCurrencyHashes.size() != NUMBER_OF_NATIVE_CURRENCY) {
            throw new CurrencyInitializationException("Failed to retrieve native currency data");
        } else {
            CurrencyData nativeCurrencyData = currencies.getByHash(nativeCurrencyHashes.iterator().next());
            if (!currencyCrypto.verifySignature(nativeCurrencyData)) {
                throw new CurrencyInitializationException("Failed to verify native currency data of " + nativeCurrencyData.getHash());
            } else {
                CurrencyTypeRegistrationData nativeCurrencyTypeRegistrationData = new CurrencyTypeRegistrationData(nativeCurrencyData);
                if (!currencyTypeRegistrationCrypto.verifySignature(nativeCurrencyTypeRegistrationData)) {
                    throw new CurrencyInitializationException("Failed to verify native currency data type of " + nativeCurrencyTypeRegistrationData.getCurrencyType().getText());
                }
            }
            if (getNativeCurrency() == null) {
                setNativeCurrencyData(nativeCurrencyData);
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

    protected void setNativeCurrencyData(CurrencyData currencyData) {
        if (this.nativeCurrencyData != null) {
            throw new CurrencyInitializationException("Attempted to override existing native currency");
        }
        this.nativeCurrencyData = currencyData;
    }

    public CurrencyData getNativeCurrency() {
        return this.nativeCurrencyData;
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

    public void putCurrencyData(CurrencyData currencyData) {
        if (currencyData == null) {
            log.error("Failed to add an empty currency");
            return;
        }
        currencies.put(currencyData);
        updateCurrencyDataIndexes(currencyData);
        updateCurrencyHashByTypeMap(currencyData);
    }

    public void updateCurrencyDataIndexes(CurrencyData currencyData) {
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

    public void removeCurrencyDataIndexes(CurrencyData currencyData) {
        // Implemented by Financial Server node
    }

    public void verifyCurrencyExists(Hash currencyDataHash) {
        if (currencies.getByHash(currencyDataHash) == null) {
            throw new CurrencyInitializationException("Failed to locate Currency " + currencyDataHash);
        }
    }

    public void setCurrencyDataName(CurrencyData currencyData, String name) {
        if (name.length() != name.trim().length()) {
            log.error("Attempted to set an invalid currency name with spaces at the start or the end {}.", name);
            return;
        }
        final String[] words = name.split(" ");
        for (String word : words) {
            if (word == null || word.isEmpty() || !Pattern.compile("[A-Za-z0-9]+").matcher(word).matches()) {
                log.error("Attempted to set an invalid currency name with the word {}.", name);
                return;
            }
        }
        currencyData.setName(name);
    }

    public void setCurrencyDataSymbol(CurrencyData currencyData, String symbol) {
        if (!Pattern.compile("[A-Z]{0,15}").matcher(symbol).matches()) {
            log.error("Attempted to set an invalid currency symbol of {}.", symbol);
            return;
        } else {
            currencyData.setSymbol(symbol);
        }
    }

    public void initTestNativeCurrencyEntry() {
        //TODO 8/22/2019 tomer: used for initial testing, creating initial Native currency
        CurrencyData currencyData = createCurrencyData("Coti Native Coin", ("CotiNative").toUpperCase(), new BigDecimal(70000), 8, Instant.now(),
                "Coti Native description", new Hash("aaaa"), new Hash("bbbb"), new Hash("cccc"), CurrencyType.NATIVE_COIN);
        putCurrencyData(currencyData);
    }

    public void initTestNonNativeCurrencyEntries() {
        //TODO 8/14/2019 tomer: used for initial testing, remove to dedicated test files
        CurrencyData currencyData = createCurrencyData("Non Native Coin1", "NONI", new BigDecimal(70000), 8, Instant.now(),
                "Coti Non Native description", new Hash("aaaa"), new Hash("bbbb"), new Hash("cccc"), CurrencyType.PAYMENT_CMD_TOKEN);
        putCurrencyData(currencyData);

        CurrencyData currencyData2 = createCurrencyData("Non Native Coin2", "NONII", new BigDecimal(70000), 8, Instant.now(),
                "Coti Non Native 2 description", new Hash("aaaa"), new Hash("bbbb"), new Hash("cccc"), CurrencyType.PAYMENT_CMD_TOKEN);
        putCurrencyData(currencyData2);
    }

    private CurrencyData createCurrencyData(String name, String symbol, BigDecimal totalSupply, int scale, Instant creationTime,
                                            String description, Hash registrarHash, Hash signerHash, Hash originatorHash, CurrencyType currencyType) {
        CurrencyData currencyData = new CurrencyData();
        setCurrencyDataName(currencyData, name);
        setCurrencyDataSymbol(currencyData, symbol);
        currencyData.setHash();
        currencyData.setTotalSupply(totalSupply);
        currencyData.setScale(scale);
        currencyData.setCreationTime(creationTime);
        currencyData.setDescription(description);
        currencyData.setRegistrarHash(registrarHash);
        currencyData.setSignerHash(signerHash);
        currencyData.setOriginatorHash(originatorHash);

        CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now(), null);
        currencyData.setCurrencyTypeData(currencyTypeData);
        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyData);
        currencyTypeRegistrationCrypto.signMessage(currencyTypeRegistrationData);
        currencyTypeData.setSignature(currencyTypeRegistrationData.getSignature());
        currencyCrypto.signMessage(currencyData);
        return currencyData;
    }


}
