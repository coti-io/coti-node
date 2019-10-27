package io.coti.financialserver.services;

import io.coti.basenode.crypto.*;
import io.coti.basenode.data.*;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IChunkService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.financialserver.crypto.GenerateTokenRequestCrypto;
import io.coti.financialserver.crypto.GetUserTokensRequestCrypto;
import io.coti.financialserver.http.GetCurrenciesRequest;
import io.coti.financialserver.http.GetCurrenciesResponse;
import io.coti.financialserver.http.GetTokenGenerationDataResponse;
import io.coti.financialserver.http.GetUserTokensRequest;
import io.coti.financialserver.model.CurrencyNameIndexes;
import io.coti.financialserver.model.PendingCurrencies;
import io.coti.financialserver.model.UserTokenGenerations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {CurrencyService.class, GetUserTokensRequestCrypto.class, NodeCryptoHelper.class, UserTokenGenerations.class,
        BaseNodeClusterStampService.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class CurrencyServiceTests {

    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private BaseNodeCurrencyService baseNodeCurrencyService;
    @Autowired
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @MockBean
    private UserTokenGenerations userTokenGenerations;
    @MockBean
    private PendingCurrencies pendingCurrencies;
    @MockBean
    private Currencies currencies;

    //
    @MockBean
    private CurrencyNameIndexes currencyNameIndexes;
    @MockBean
    protected INetworkService networkService;
    @MockBean
    private GenerateTokenRequestCrypto generateTokenRequestCrypto;
    @MockBean
    private CurrencyOriginatorCrypto currencyOriginatorCrypto;
    @MockBean
    private CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @MockBean
    private Transactions transactions;

    //from base node
    @MockBean
    protected RestTemplate restTemplate;
    @MockBean
    protected ApplicationContext applicationContext;
    @MockBean
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @MockBean
    private HttpJacksonSerializer jacksonSerializer;
    @MockBean
    private CurrencyRegistrarCrypto currencyRegistrarCrypto;
    @MockBean
    private IChunkService chunkService;
    @MockBean
    private BaseNodeClusterStampService baseNodeClusterStampService;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private TransactionHelper transactionHelper;

    //
    private static Hash userHash;
    private static Hash currencyHash;
    private static Hash currencyHash2;
    private static Hash transactionHash;
    private static CurrencyData currencyData;
    private static CurrencyData currencyData2;
    private static CurrencyData nativeCurrencyData;
    private static Hash nativeCurrencyHash;
    public static final String RECOVER_NATIVE_CURRENCY_PATH = "null/currencies/native";


    @BeforeClass
    public static void setUpOnce() {
        userHash = new Hash("00");
        transactionHash = new Hash("11");
        currencyHash = new Hash("22");
        currencyHash2 = new Hash("33");

        currencyData = new CurrencyData();
        currencyData.setHash(currencyHash);
        currencyData.setName("AlexToken");
        currencyData.setSymbol("ALX");
        currencyData.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.PAYMENT_CMD_TOKEN, Instant.now()));
        currencyData.setDescription("Dummy token for testing");
        currencyData.setTotalSupply(new BigDecimal("100"));
        currencyData.setScale(8);
        currencyData.setCreationTime(Instant.now());

        currencyData2 = new CurrencyData();
        currencyData2.setHash(currencyHash2);
        currencyData2.setName("Tomeroken");
        currencyData2.setSymbol("TMR");
        currencyData2.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.PAYMENT_CMD_TOKEN, Instant.now()));
        currencyData2.setDescription("Dummy token for testing");
        currencyData2.setTotalSupply(new BigDecimal("100"));
        currencyData2.setScale(8);
        currencyData2.setCreationTime(Instant.now());

        nativeCurrencyHash = new Hash(7777);
        nativeCurrencyData = new CurrencyData();
        nativeCurrencyData.setHash(nativeCurrencyHash);
        nativeCurrencyData.setName("Coti");
        nativeCurrencyData.setSymbol("COTI");
        nativeCurrencyData.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.NATIVE_COIN, Instant.now()));
        nativeCurrencyData.setDescription("native description");
        nativeCurrencyData.setTotalSupply(new BigDecimal(20000));
        nativeCurrencyData.setScale(8);
        nativeCurrencyData.setCreationTime(Instant.now());
    }

    @Before
    public void setUpBeforeEachTest() {

    }

    //TODO 9/20/2019 astolia: clean up, remove duplicates...

    @Test
    public void getUserTokenGenerationData_UnsignedRequest_shouldReturnBadRequest() {
        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        ResponseEntity expected = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));

        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);
        Assert.assertEquals(expected, actual);

        getUserTokensRequest.setUserHash(new Hash("1111"));
        actual = currencyService.getUserTokens(getUserTokensRequest);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_noUserTokenGenerations_shouldReturnEmptyResponse() {
        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        //TODO 9/24/2019 astolia: mock signed request
        //getUserTokensRequestCrypto.signMessage(getUserTokensRequest);
        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);
        ResponseEntity expected = ResponseEntity.ok(new GetTokenGenerationDataResponse());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_onlyPendingToken_shouldReturnResponseWithAvailableTransaction() {
        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        //TODO 9/24/2019 astolia: mock signed request
//        getUserTokensRequestCrypto.signMessage(getUserTokensRequest);
        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);

        GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
        //TODO 9/24/2019 astolia: change according to implementation change
//        getTokenGenerationDataResponse.addUnusedConfirmedTransaction(transactionHash);
        ResponseEntity expected = ResponseEntity.ok(getTokenGenerationDataResponse);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_onlyCompletedToken_shouldReturnResponseWithAvailableTransactionAndCurrency() {
        when(currencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, currencyData.getHash());

        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        //TODO 9/24/2019 astolia: mock signed request
//        getUserTokensRequestCrypto.signMessage(getUserTokensRequest);
        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);

        GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
        //TODO 9/24/2019 astolia: change according to implementation change
//        getTokenGenerationDataResponse.addCompletedTransactionHashToGeneratedCurrency(transactionHash, currencyData);
        ResponseEntity expected = ResponseEntity.ok(getTokenGenerationDataResponse);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_onlyPendingToken_shouldReturnResponseWithPendingTransactionAndCurrency() {

        when(pendingCurrencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, currencyData.getHash());

        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        //TODO 9/24/2019 astolia: mock signed request
//        getUserTokensRequestCrypto.signMessage(getUserTokensRequest);
        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);

        GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
        //TODO 9/24/2019 astolia: change according to implementation change
//        getTokenGenerationDataResponse.addPendingTransactionHashToGeneratedCurrency(transactionHash, currencyData);
        ResponseEntity expected = ResponseEntity.ok(getTokenGenerationDataResponse);
        Assert.assertEquals(expected, actual);
    }

    //TODO 9/22/2019 astolia: run TransactionService continueHandlePropagatedTransaction with TokenGenerationTransaction before some tests to mock insertion of data to db.

    protected void currencyServiceInit() {
        when(currencies.isEmpty()).thenReturn(Boolean.TRUE);
        when(restTemplate.getForObject(RECOVER_NATIVE_CURRENCY_PATH, CurrencyData.class)).thenReturn(nativeCurrencyData);
        currencyService.init();
    }

    @Test
    public void getTokens() {
        currencyServiceInit();

        List<Hash> currenciesList = Arrays.asList(currencyData2.getHash(), currencyData.getHash());

        GetCurrenciesRequest getCurrenciesRequest = new GetCurrenciesRequest();
        getCurrenciesRequest.setTokenHashes(currenciesList);

        when(currencies.getByHash(currencyData2.getHash())).thenReturn(currencyData2);
        when(currencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        ResponseEntity<IResponse> tokens = currencyService.getCurrenciesForWallet(getCurrenciesRequest);
        Assert.assertTrue(tokens.getStatusCode().equals(HttpStatus.OK));
        Assert.assertTrue(((GetCurrenciesResponse) tokens.getBody()).getTokens().get(0).getName().equals(currencyData.getName()));
        Assert.assertTrue(((GetCurrenciesResponse) tokens.getBody()).getTokens().get(1).getName().equals(currencyData2.getName()));
    }
}
