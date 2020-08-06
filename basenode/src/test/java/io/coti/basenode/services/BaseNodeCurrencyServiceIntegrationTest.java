package io.coti.basenode.services;

import io.coti.basenode.communication.*;
import io.coti.basenode.communication.interfaces.*;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.CurrencyTypeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.http.interfaces.ISerializer;
import io.coti.basenode.model.*;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import testUtils.BaseNodeTestUtils;

import java.time.Instant;

import static testUtils.CurrencyServiceTestUtils.createCurrencyData;

@ContextConfiguration(classes = {BaseNodeCurrencyService.class, BaseNodeRocksDBConnector.class,
        Currencies.class, NodeCryptoHelper.class,
        Transactions.class, AddressTransactionsHistories.class, TransactionIndexes.class, TransactionVotes.class,
        NodeRegistrations.class, Addresses.class,
        BaseNodeNetworkService.class,
        RestTemplate.class, ApplicationContext.class, CommunicationService.class,
        ZeroMQReceiver.class, ZeroMQSubscriber.class, ZeroMQPropagationPublisher.class, ZeroMQSender.class,
        HttpJacksonSerializer.class, JacksonSerializer.class, ZeroMQSubscriberHandler.class,
})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeCurrencyServiceIntegrationTest {

    public static final String CURRENCY_HASH_STRING_EXAMPLE = "022dc3f71c4033ebabcec23beb56eeb3d29f3e58a8fdb0958ee16ec28cde4606f4e94b1e2507af4cd852ced100a8465ae142cf9e1981d86021fb32a0d0e213bf7aafa4d4";
    public static final String SIGNER_HASH_STRING_EXAMPLE = "021dc3f71c4033ebabcec23beb56eeb3d29f3e58a8fdb0958ee16ec28cde4606f4e94b1e2507af4cd852ced100a8465ae142cf9e1981d86021fb32a0d0e213bf7aafa4d4";
    @Autowired
    private BaseNodeCurrencyService baseNodeCurrencyService;
    @Autowired
    private BaseNodeRocksDBConnector baseNodeRocksDBConnector;
    @Autowired
    public IDatabaseConnector databaseConnector;
    @Autowired
    private Currencies currencies;
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private BaseNodeNetworkService baseNodeNetworkService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    protected IReceiver receiver;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ISender sender;
    @Autowired
    private ISerializer iSerializer;
    @Autowired
    private ISubscriberHandler iSubscriberHandler;
    @MockBean
    private ITransactionService transactionService;
    @MockBean
    private IAddressService addressService;
    @MockBean
    private IDspVoteService dspVoteService;
    @Autowired
    private INetworkService networkService;
    @MockBean
    private NetworkNodeCrypto networkNodeCrypto;
    @MockBean
    private NodeRegistrationCrypto nodeRegistrationCrypto;

    @Before
    public void init() {
        baseNodeRocksDBConnector.init();
        baseNodeCurrencyService.init();
    }

    //    @Test
    public void updateCurrencyHashByTypeMapFromExistingCurrencies_addNewCurrency_mapByTypeReflectsNewCurrency() {
        CurrencyType currencyType = CurrencyType.REGULAR_CMD_TOKEN;
        Hash currencyHash = BaseNodeTestUtils.generateRandomHash(136);
        CurrencyData currencyData = createCurrencyData("Coti Test name", "Coti Test Symbol", currencyHash);
        setAndSignCurrencyDataByType(currencyData, currencyType);
        currencies.put(currencyData);
//        final int numberOfCurrenciesByTypePreUpdate =
//                baseNodeCurrencyService.getCurrencyHashesByCurrencyType(currencyType) != null ? baseNodeCurrencyService.getCurrencyHashesByCurrencyType(currencyType).size() : 0;

//        baseNodeCurrencyService.setNativeCurrencyFromExistingCurrencies();

//        final int numberOfCurrenciesByTypePostUpdate = baseNodeCurrencyService.getCurrencyHashesByCurrencyType(currencyType).size();
//        Assert.assertEquals(numberOfCurrenciesByTypePostUpdate - 1, numberOfCurrenciesByTypePreUpdate);
//        Assert.assertTrue(baseNodeCurrencyService.getCurrencyHashesByCurrencyType(currencyType).contains(currencyData.getHash()));
    }

    //    @Test
    public void replaceExistingCurrencyDataDueToTypeChange_addNewCurrencyUpdateCurrencyType_verifyUpdatedCurrencyType() {
        CurrencyType currencyType = CurrencyType.REGULAR_CMD_TOKEN;
        Hash currencyHash = BaseNodeTestUtils.generateRandomHash(136);
        CurrencyData currencyData = createCurrencyData("Coti Test name", "Coti Test Symbol", currencyHash);
        setAndSignCurrencyDataByType(currencyData, currencyType);
        currencies.put(currencyData);
//        final int numberOfCurrenciesByTypePreUpdate =
//                baseNodeCurrencyService.getCurrencyHashesByCurrencyType(currencyType) != null ? baseNodeCurrencyService.getCurrencyHashesByCurrencyType(currencyType).size() : 0;

//        baseNodeCurrencyService.setNativeCurrencyFromExistingCurrencies();

//        final int numberOfCurrenciesByTypePostUpdate = baseNodeCurrencyService.getCurrencyHashesByCurrencyType(currencyType).size();
//        Assert.assertEquals(numberOfCurrenciesByTypePostUpdate - 1, numberOfCurrenciesByTypePreUpdate);
//        Assert.assertTrue(baseNodeCurrencyService.getCurrencyHashesByCurrencyType(currencyType).contains(currencyData.getHash()));

        CurrencyType newCurrencyType = CurrencyType.REGULAR_CMD_TOKEN;
        Hash newCurrencyHash = BaseNodeTestUtils.generateRandomHash(136);
        CurrencyData newCurrencyData = createCurrencyData("Coti Test name", "Coti Test Symbol", newCurrencyHash);
        setAndSignCurrencyDataByType(newCurrencyData, newCurrencyType);

//        baseNodeCurrencyService.replaceExistingCurrencyDataDueToTypeChange(currencyData, newCurrencyData);
//        Assert.assertTrue(baseNodeCurrencyService.getCurrencyHashesByCurrencyType(newCurrencyType).contains(newCurrencyData.getHash()));
    }

    //    @Test
    public void getNativeCurrencyData_addNativeCurrencyIfNeeded_verifyCurrencyExists() {
        final CurrencyData nativeCurrencyData = baseNodeCurrencyService.getNativeCurrency();
        if (nativeCurrencyData != null) {
            Assert.assertEquals(CurrencyType.NATIVE_COIN, nativeCurrencyData.getCurrencyTypeData().getCurrencyType());
        } else {
//            HashSet nativeCurrencyHashes = baseNodeCurrencyService.getCurrencyHashesByCurrencyType(CurrencyType.NATIVE_COIN);
//            Assert.assertTrue(nativeCurrencyHashes == null || nativeCurrencyHashes.isEmpty());
            CurrencyType currencyType = CurrencyType.NATIVE_COIN;
            Hash currencyHash = BaseNodeTestUtils.generateRandomHash(136);
            CurrencyData currencyData = createCurrencyData("Coti Test name", "Coti Test Symbol", currencyHash);
            setAndSignCurrencyDataByType(currencyData, currencyType);
            baseNodeCurrencyService.putCurrencyData(currencyData);
            Assert.assertEquals(baseNodeCurrencyService.getNativeCurrency(), currencyData);
        }
    }

    //    @Test
    public void getMissingCurrencies_noPreexistingCurrencies_noReturnedCurrencies() {
        int iPause = 7;
//        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
//        Set<Hash> missingCurrencyHashes = new HashSet<>();
//        getCurrencyRequest.setCurrenciesHashes(missingCurrencyHashes);
//
//        Hash signerHash = new Hash(SIGNER_HASH_STRING_EXAMPLE);
//        getCurrencyRequest.setSignerHash(signerHash);
//        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);

//        ResponseEntity<BaseResponse> missingCurrencies = baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
//
//        Assert.assertEquals(missingCurrencies.getStatusCode(), (HttpStatus.OK));
//        Assert.assertTrue(((GetCurrencyResponse) missingCurrencies.getBody()).getCurrencyDataSet().isEmpty());
    }

    //    @Test
    public void getMissingCurrencies_badSignature_unauthorized() {
//        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
//        Set<Hash> missingCurrencyHashes = new HashSet<>();
//        getCurrencyRequest.setCurrenciesHashes(missingCurrencyHashes);
//
//        Hash signerHash = new Hash(SIGNER_HASH_STRING_EXAMPLE);
//        getCurrencyRequest.setSignerHash(signerHash);
//        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);
//        missingCurrencyHashes.add(BaseNodeTestUtils.generateRandomHash());

//        ResponseEntity<BaseResponse> missingCurrencies = baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
//
//        Assert.assertEquals(missingCurrencies.getStatusCode(), (HttpStatus.UNAUTHORIZED));
//        Assert.assertTrue(((GetCurrencyResponse) missingCurrencies.getBody()).getCurrencyDataSet().isEmpty());
    }

    //    @Test
    public void getMissingCurrencies_singlePreexistingCurrencies_singleReturnedCurrencies() {
//        Hash currencyHash = new Hash(CURRENCY_HASH_STRING_EXAMPLE);
//
//        CurrencyData currencyData = BaseNodeTestUtils.createCurrencyData("Coti Test name", "Coti Test Symbol", currencyHash);
//        currencyCrypto.signMessage(currencyData);
//        baseNodeCurrencyService.putCurrencyData(currencyData);
//
//        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
//        Set<Hash> missingCurrencyHashes = new HashSet<>();
//        missingCurrencyHashes.add(currencyData.getHash());
//        getCurrencyRequest.setCurrenciesHashes(missingCurrencyHashes);
//
//        Hash signerHash = new Hash(SIGNER_HASH_STRING_EXAMPLE);
//        getCurrencyRequest.setSignerHash(signerHash);
//        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);

//        ResponseEntity<BaseResponse> missingCurrencies = baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
//
//        Assert.assertEquals(missingCurrencies.getStatusCode(), (HttpStatus.OK));
//        Assert.assertTrue(((GetCurrencyResponse) missingCurrencies.getBody()).getCurrencyDataSet().contains(currencyData));
    }

    //    @Test
    public void getMissingCurrencies_updatingMissingCurrencies_removedMissingCurrencies() {
        Hash currencyHash = new Hash(CURRENCY_HASH_STRING_EXAMPLE);
        CurrencyData currencyData = createCurrencyData("Coti Test name", "Coti Test Symbol", currencyHash);
        CurrencyType currencyType = CurrencyType.NATIVE_COIN;
        setAndSignCurrencyDataByType(currencyData, currencyType);


//        currencyRegistrarCrypto.signMessage(currencyData);
        currencies.delete(currencyData);
//        baseNodeCurrencyService.updateMissingCurrencyDataHashesFromClusterStamp(currencyHash);
//        Assert.assertTrue(baseNodeCurrencyService.getMissingCurrencyDataHashes().contains(currencyHash));
//
//        baseNodeCurrencyService.putCurrencyData(currencyData);
//
//        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
//        Set<Hash> missingCurrencyHashes = new HashSet<>();
//        missingCurrencyHashes.add(currencyData.getHash());
//        getCurrencyRequest.setCurrenciesHashes(missingCurrencyHashes);
//
//        Hash signerHash = new Hash(SIGNER_HASH_STRING_EXAMPLE);
//        getCurrencyRequest.setSignerHash(signerHash);
//        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);
//
//        ResponseEntity<BaseResponse> missingCurrencies = baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
//        baseNodeCurrencyService.removeHashFromMissingCurrencyDataHashes(currencyHash);
//
//        Assert.assertEquals(missingCurrencies.getStatusCode(), (HttpStatus.OK));
//        Assert.assertTrue(((GetCurrencyResponse) missingCurrencies.getBody()).getCurrencyDataSet().contains(currencyData));
//
//        Assert.assertFalse(baseNodeCurrencyService.getMissingCurrencyDataHashes().contains(currencyHash));
    }

    protected void setAndSignCurrencyDataByType(CurrencyData currencyData, CurrencyType currencyType) {
        CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now());
//        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyData.getHash(),
//                currencyType, Instant.now());
//        currencyTypeCrypto.signMessage(currencyTypeData);
//        currencyTypeRegistrationCrypto.signMessage(currencyTypeRegistrationData);
        currencyData.setCurrencyTypeData(currencyTypeData);
//        currencyRegistrarCrypto.signMessage(currencyData);
    }

}
