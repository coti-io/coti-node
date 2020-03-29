package io.coti.basenode.services;

import io.coti.basenode.crypto.CurrencyRegistrarCrypto;
import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.GetUpdatedCurrencyRequestCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.http.CustomRequestCallBack;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IChunkService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.FluxSink;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Service
public class BaseNodeCurrencyService implements ICurrencyService {

    private static final int MAXIMUM_BUFFER_SIZE = 50000;
    private static final String RECOVERY_NODE_GET_CURRENCIES_UPDATE_REACTIVE_ENDPOINT = "/currencies/update/batch";
    private static final int NUMBER_OF_NATIVE_CURRENCY = 1;
    private EnumMap<CurrencyType, HashSet<Hash>> currencyHashByTypeMap;
    protected CurrencyData nativeCurrencyData;
    @Autowired
    protected Currencies currencies;
    @Autowired
    protected CurrencyNameIndexes currencyNameIndexes;
    @Autowired
    protected INetworkService networkService;
    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @Autowired
    protected CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @Autowired
    private HttpJacksonSerializer jacksonSerializer;
    @Autowired
    private OriginatorCurrencyCrypto originatorCurrencyCrypto;
    @Autowired
    private CurrencyRegistrarCrypto currencyRegistrarCrypto;
    @Autowired
    private IChunkService chunkService;
    @Autowired
    private BaseNodeClusterStampService baseNodeClusterStampService;
    @Autowired
    protected IBalanceService balanceService;

    public void init() {
        try {
            currencyHashByTypeMap = new EnumMap<>(CurrencyType.class);
            nativeCurrencyData = null;
            updateCurrencyHashByTypeMapFromExistingCurrencies();
            updateCurrencies();
            log.info("{} is up", this.getClass().getSimpleName());
        } catch (CurrencyException e) {
            throw new CurrencyException("Error at currency service init.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new CurrencyException("Error at currency service init.", e);
        }
    }

    public void updateCurrencyHashByTypeMapFromExistingCurrencies() {
        currencies.forEach(this::updateCurrencyHashByTypeMap);
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

    @Override
    public void updateCurrencies() {
        GetUpdatedCurrencyRequest getUpdatedCurrencyRequest = new GetUpdatedCurrencyRequest();
        getUpdatedCurrencyRequest.setCurrencyHashesByType(currencyHashByTypeMap);
        getUpdatedCurrencyRequestCrypto.signMessage(getUpdatedCurrencyRequest);
        getUpdatedCurrencyDataFromRecoveryServer(getUpdatedCurrencyRequest);

        verifyValidNativeCurrencyPresent();
    }

    private void getUpdatedCurrencyDataFromRecoveryServer(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest) {
        CustomRequestCallBack requestCallBack = new CustomRequestCallBack(jacksonSerializer, getUpdatedCurrencyRequest);
        ResponseExtractor responseExtractor = chunkService.getResponseExtractor(currencyData -> currencyDataFromRecoveryServiceHandler((CurrencyData) currencyData), MAXIMUM_BUFFER_SIZE);
        restTemplate.execute(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_CURRENCIES_UPDATE_REACTIVE_ENDPOINT,
                HttpMethod.POST, requestCallBack, responseExtractor);
    }

    private void currencyDataFromRecoveryServiceHandler(CurrencyData currencyData) {
        CurrencyData originalCurrencyData = currencies.getByHash(currencyData.getHash());
        if (originalCurrencyData != null) {
            replaceExistingCurrencyDataDueToTypeChange(originalCurrencyData, currencyData);
        } else {
            putCurrencyData(currencyData);
        }
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
            throw new CurrencyException("Failed to retrieve native currency data");
        } else {
            Hash nativeCurrencyHash = nativeCurrencyHashes.iterator().next();
            CurrencyData nativeCurrency = currencies.getByHash(nativeCurrencyHash);
            if (!originatorCurrencyCrypto.verifySignature(nativeCurrency)) {
                throw new CurrencyException("Failed to verify native currency data of " + nativeCurrency.getHash());
            } else {
                CurrencyTypeData naticeCurrencyTypeData = nativeCurrency.getCurrencyTypeData();
                CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(nativeCurrencyHash, naticeCurrencyTypeData);
                if (!currencyTypeRegistrationCrypto.verifySignature(currencyTypeRegistrationData)) {
                    throw new CurrencyException("Failed to verify native currency data type of " + nativeCurrency.getCurrencyTypeData().getCurrencyType().getText());
                }
            }
            if (getNativeCurrency() == null) {
                setNativeCurrencyData(nativeCurrency);
            }
        }
    }

    protected void setNativeCurrencyData(CurrencyData currencyData) {
        if (this.nativeCurrencyData != null) {
            throw new CurrencyException("Attempted to override existing native currency");
        }
        this.nativeCurrencyData = currencyData;
    }

    @Override
    public CurrencyData getNativeCurrency() {
        return this.nativeCurrencyData;
    }

    @Override
    public Hash getNativeCurrencyHash() {
        if (nativeCurrencyData == null) {
            throw new CurrencyException("Native currency is missing.");
        }
        return nativeCurrencyData.getHash();
    }

    @Override
    public void getUpdatedCurrencyBatch(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest, FluxSink<CurrencyData> fluxSink) {
        try {
            if (!getUpdatedCurrencyRequestCrypto.verifySignature(getUpdatedCurrencyRequest)) {
                log.error("Authorization check failed on request to get updated currencies.");
            }
            Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType = getUpdatedCurrencyRequest.getCurrencyHashesByType();
            getRequiringUpdateOfCurrencyDataByType(existingCurrencyHashesByType, fluxSink);
        } finally {
            fluxSink.complete();
        }
    }

    @Override
    public CurrencyData getCurrencyFromDB(Hash currencyHash) {
        return currencies.getByHash(currencyHash);
    }

    @Override
    public void handleInitiatedTokenNotice(InitiatedTokenNoticeData initiatedTokenNoticeData) {
        CurrencyData currencyData = initiatedTokenNoticeData.getCurrencyData();
        if (!validateInitiatedToken(currencyData)) {
            return;
        }
        putCurrencyData(currencyData);
        baseNodeClusterStampService.handleInitiatedTokenNotice(initiatedTokenNoticeData);
    }

    @Override
    public void generateNativeCurrency() {
        throw new CurrencyException("Attempted to generate Native currency.");
    }

    @Override
    public void updateCurrenciesFromClusterStamp(Map<Hash, CurrencyData> clusterStampCurrenciesMap, Hash genesisAddress) {
        clusterStampCurrenciesMap.forEach((currencyHash, clusterStampCurrencyData) ->
                currencies.put(clusterStampCurrencyData)
        );
    }

    private boolean validateInitiatedToken(CurrencyData currencyData) {
        if (!originatorCurrencyCrypto.verifySignature(currencyData)) {
            log.error("Failed to verify propagated currency {} originator already exists", currencyData.getName());
            return false;
        }
        if (!currencyRegistrarCrypto.verifySignature(currencyData)) {
            log.error("Failed to verify propagated currency {} registrar already exists", currencyData.getName());
            return false;
        }
        if (verifyCurrencyExists(currencyData.getHash())) {
            log.error("Propagated currency {} already exists", currencyData.getName());
            return false;
        }
        if (currencyData.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN)) {
            log.error("Propagated currency {} marked as native", currencyData.getName());
            return false;
        }
        return true;
    }

    private void getRequiringUpdateOfCurrencyDataByType(Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType, FluxSink<CurrencyData> fluxSink) {
        currencyHashByTypeMap.forEach((localCurrencyType, localCurrencyHashes) -> {
            HashSet<Hash> existingCurrencyHashes = existingCurrencyHashesByType.get(localCurrencyType);

            localCurrencyHashes.forEach(localCurrencyHash -> {
                if (existingCurrencyHashes == null || existingCurrencyHashes.isEmpty() ||
                        !existingCurrencyHashes.contains(localCurrencyHash)) {
                    sendUpdatedCurrencyData(localCurrencyHash, fluxSink);
                }
            });
        });
    }

    private void sendUpdatedCurrencyData(Hash localCurrencyHash, FluxSink<CurrencyData> fluxSink) {
        CurrencyData updatedCurrencyData = currencies.getByHash(localCurrencyHash);
        if (updatedCurrencyData != null) {
            fluxSink.next(updatedCurrencyData);
        } else {
            throw new CurrencyException(String.format("Failed to retrieve currencyData %s", localCurrencyHash));
        }
    }

    @Override
    public void putCurrencyData(CurrencyData currencyData) {
        if (currencyData == null) {
            throw new CurrencyException("Failed to add an empty currency");
        }
        currencies.put(currencyData);
        updateCurrencyHashByTypeMap(currencyData);
        updateCurrencyNameIndex(currencyData);
    }

    private void updateCurrencyNameIndex(CurrencyData currencyData) {
        currencyNameIndexes.put(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
    }

    public boolean verifyCurrencyExists(Hash currencyDataHash) {
        return currencies.getByHash(currencyDataHash) != null;
    }

    protected void validateCurrencyUniqueness(Hash currencyHash, String currencyName) {
        if (currencyNameIndexes.getByHash(new CurrencyNameIndexData(currencyName, currencyHash).getHash()) != null) {
            throw new CurrencyException("Currency name is already in use.");
        }
        if (currencies.getByHash(currencyHash) != null) {
            throw new CurrencyException("Currency symbol is already in use.");
        }
    }
}
