package io.coti.basenode.services;


import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.GetUserTokensRequestCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.model.UserCurrencyIndexes;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IEventService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;


@ContextConfiguration(classes = {BaseNodeCurrencyService.class, BaseNodeRocksDBConnector.class,
        Currencies.class, NodeCryptoHelper.class,
        BaseNodeNetworkService.class, RestTemplate.class,
        ApplicationContext.class,
        CurrencyTypeRegistrationCrypto.class
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeCurrencyServiceTest {

    @Autowired
    private BaseNodeCurrencyService baseNodeCurrencyService;
    @MockBean
    protected CurrencyNameIndexes currencyNameIndexes;
    @MockBean
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    @MockBean
    protected IBalanceService balanceService;
    @MockBean
    private UserCurrencyIndexes userCurrencyIndexes;
    @MockBean
    protected IEventService eventService;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private Transactions transactions;
    @MockBean
    private BaseNodeRocksDBConnector baseNodeRocksDBConnector;
    @MockBean
    private NodeCryptoHelper nodeCryptoHelper;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @MockBean
    private ApplicationContext applicationContext;
    @MockBean
    private Currencies currencies;
    @MockBean
    private BaseNodeNetworkService baseNodeNetworkService;
    @MockBean
    private TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData;
    @MockBean
    private TokenGenerationServiceData tokenGenerationServiceData;
    @MockBean
    private OriginatorCurrencyData currencyData;

    @Before
    public void init() {
        baseNodeCurrencyService.init();
    }

    @Test
    public void revertCurrencyUnconfirmedRecord() {
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

//    @Test
//    public void getNativeCurrencyData_addNativeCurrencyIfNeeded_verifyCurrencyExists() {
//
//
//        final CurrencyData nativeCurrencyData = baseNodeCurrencyService.getNativeCurrency();
//        if (nativeCurrencyData != null) {
//            Assert.assertEquals(CurrencyType.NATIVE_COIN, nativeCurrencyData.getCurrencyTypeData().getCurrencyType());
//        } else {
//            HashSet nativeCurrencyHashes = baseNodeCurrencyService.getCurrencyHashesByCurrencyType(CurrencyType.NATIVE_COIN);
//            Assert.assertTrue(nativeCurrencyHashes == null || nativeCurrencyHashes.isEmpty());
//            CurrencyType currencyType = CurrencyType.NATIVE_COIN;
//            Hash currencyHash = BaseNodeTestUtils.generateRandomHash(136);
//            CurrencyData currencyData = createCurrencyData("Coti Test name", "Coti Test Symbol", currencyHash);
//            setAndSignCurrencyDataByType(currencyData, currencyType);
//            baseNodeCurrencyService.putCurrencyData(currencyData);
//            Assert.assertEquals(baseNodeCurrencyService.getNativeCurrency(), currencyData);
//        }
//    }

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
