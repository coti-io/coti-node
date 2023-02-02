package io.coti.basenode.services;


import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.model.UserCurrencyIndexes;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IEventService;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import testUtils.BaseNodeTestUtils;

import java.util.HashSet;
import java.util.Set;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static org.mockito.Mockito.*;


@ContextConfiguration(classes = {BaseNodeCurrencyService.class, BaseNodeRocksDBConnector.class,
        Currencies.class,
        BaseNodeNetworkService.class, RestTemplate.class, ApplicationContext.class,
        CurrencyTypeRegistrationCrypto.class
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeCurrencyServiceTest {

    @Autowired
    private BaseNodeCurrencyService baseNodeCurrencyService;
    @MockBean
    protected CurrencyNameIndexes currencyNameIndexesLocal;
    @MockBean
    protected IBalanceService balanceService;
    @MockBean
    private UserCurrencyIndexes userCurrencyIndexesLocal;
    @MockBean
    protected IEventService eventService;
    @MockBean
    private BaseNodeTransactionHelper transactionHelper;
    @MockBean
    private Currencies currenciesLocal;
    @MockBean
    private BaseNodeNetworkService baseNodeNetworkService;
    @MockBean
    private TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData;
    @MockBean
    private TokenGenerationServiceData tokenGenerationServiceData;
    @MockBean
    private OriginatorCurrencyData currencyData;

    @BeforeEach
    void init() {
        baseNodeCurrencyService.init();
        nodeTransactionHelper = transactionHelper;
        nodeEventService = eventService;
        currencies = currenciesLocal;
        currencyNameIndexes = currencyNameIndexesLocal;
        userCurrencyIndexes = userCurrencyIndexesLocal;
    }

    @Test
    void revertCurrencyUnconfirmedRecord() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Hash originatorHash = TransactionTestUtils.generateRandomHash();
        Set<Hash> tokensHashSet = new HashSet<>();
        UserCurrencyIndexData userCurrencyIndexData = new UserCurrencyIndexData(originatorHash, tokensHashSet);
        Hash currencyHash = OriginatorCurrencyCrypto.calculateHash("COTI");
        when(transactionHelper.getTokenGenerationFeeData(transactionData)).thenReturn(tokenGenerationFeeBaseTransactionData);
        when(tokenGenerationFeeBaseTransactionData.getServiceData()).thenReturn(tokenGenerationServiceData);
        when(tokenGenerationServiceData.getOriginatorCurrencyData()).thenReturn(currencyData);
        when(currencyData.getSymbol()).thenReturn("COTI");
        when(currencyData.getName()).thenReturn("coti native");
        when(currencyData.getOriginatorHash()).thenReturn(originatorHash);
        when(userCurrencyIndexes.getByHash(originatorHash)).thenReturn(userCurrencyIndexData);
        baseNodeCurrencyService.revertCurrencyUnconfirmedRecord(transactionData);
        verify(currencies, atLeastOnce()).deleteByHash(currencyHash);
        verify(userCurrencyIndexes, atLeastOnce()).deleteByHash(originatorHash);
    }

    @Test
    void get_native_currency_data_if_null() {
        Hash calculatedCurrencyHash = OriginatorCurrencyCrypto.calculateHash("COTI");
        Hash nativeCurrencyHash = baseNodeCurrencyService.getNativeCurrencyHashIfNull(null);
        Assertions.assertEquals(nativeCurrencyHash, calculatedCurrencyHash);
    }

    @Test
    void is_currency_hash_allowed() {
        Hash currencyHash = BaseNodeTestUtils.generateRandomHash(136);
        Assertions.assertFalse(baseNodeCurrencyService.isCurrencyHashAllowed(currencyHash));
    }

//    @Test
//    public void getMissingCurrencies_noPreexistingCurrencies_noReturnedCurrencies() {
//        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
//        Set<Hash> missingCurrencyHashes = new HashSet<>();
//        getCurrencyRequest.setCurrenciesHashes(missingCurrencyHashes);
//
//        Hash signerHash = new Hash("PublicKey");
//        getCurrencyRequest.setSignerHash(signerHash);
//        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);
//
//        ResponseEntity<BaseResponse> missingCurrencies = baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
//
//        Assert.assertEquals(missingCurrencies.getStatusCode(), (HttpStatus.OK));
//        Assert.assertTrue(((GetCurrencyResponse) missingCurrencies.getBody()).getCurrencyDataSet().isEmpty());
//    }

//    @Test
//    public void getMissingCurrencies_badSignature_unauthorized() {
//        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
//        Set<Hash> missingCurrencyHashes = new HashSet<>();
//        getCurrencyRequest.setCurrenciesHashes(missingCurrencyHashes);
//
//        Hash signerHash = new Hash("PublicKey");
//        getCurrencyRequest.setSignerHash(signerHash);
//        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);
//        missingCurrencyHashes.add(BaseNodeTestUtils.generateRandomHash());
//
//        ResponseEntity<BaseResponse> missingCurrencies = baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
//
//        Assert.assertEquals(missingCurrencies.getStatusCode(), (HttpStatus.UNAUTHORIZED));
//        Assert.assertEquals(missingCurrencies.getBody().getStatus(),STATUS_ERROR);
//    }

//    protected void setAndSignCurrencyDataByType(CurrencyData currencyData, CurrencyType currencyType) {
//        CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now());
//        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyData.getSymbol(), currencyTypeData);
//        currencyTypeCrypto.signMessage(currencyTypeData);
//        currencyTypeRegistrationCrypto.signMessage(currencyTypeData);
//        currencyData.setCurrencyTypeData(currencyTypeData);
//        currencyRegistrarCrypto.signMessage(currencyData);
//    }

}
