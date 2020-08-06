package io.coti.financialserver.services;

import io.coti.basenode.crypto.GetUserTokensRequestCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetUserTokensRequest;
import io.coti.basenode.http.GetUserTokensResponse;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IChunkService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.financialserver.http.GetCurrenciesRequest;
import io.coti.financialserver.http.GetCurrenciesResponse;
import io.coti.financialserver.model.UserTokenGenerations;
import org.junit.Assert;
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
    private static CurrencyService currencyService;
    @Autowired
    private BaseNodeCurrencyService baseNodeCurrencyService;
    @Autowired
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @MockBean
    private UserTokenGenerations userTokenGenerations;
    @MockBean
    private Currencies currencies;

    //
    @MockBean
    private CurrencyNameIndexes currencyNameIndexes;
    @MockBean
    protected INetworkService networkService;
    @MockBean
    private OriginatorCurrencyCrypto originatorCurrencyCrypto;
    @MockBean
    private Transactions transactions;

    //from base node
    @MockBean
    protected RestTemplate restTemplate;
    @MockBean
    protected ApplicationContext applicationContext;
    @MockBean
    private HttpJacksonSerializer jacksonSerializer;
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
    private static Hash transactionHash;
    private static CurrencyData currencyData;
    private static CurrencyData currencyData2;
    private static CurrencyData nativeCurrencyData;
    public static final String RECOVER_NATIVE_CURRENCY_PATH = "null/currencies/native";


    @BeforeClass
    public static void setUpOnce() {
        userHash = new Hash("00");
        transactionHash = new Hash("11");
        Hash currencyHash = new Hash("22");
        Hash currencyHash2 = new Hash("33");

        currencyData = new CurrencyData();
        currencyData.setHash(currencyHash);
        currencyData.setName("AlexToken");
        currencyData.setSymbol("ALX");
        currencyService.validateName(currencyData);
        currencyService.validateSymbol(currencyData);
        currencyData.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.PAYMENT_CMD_TOKEN, Instant.now()));
        currencyData.setDescription("Dummy token for testing");
        currencyData.setTotalSupply(new BigDecimal("100"));
        currencyData.setScale(8);
        currencyData.setCreateTime(Instant.now());

        currencyData2 = new CurrencyData();
        currencyData2.setHash(currencyHash2);
        currencyData2.setName("Tomeroken");
        currencyData2.setSymbol("TMR");
        currencyService.validateName(currencyData2);
        currencyService.validateSymbol(currencyData2);
        currencyData2.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.PAYMENT_CMD_TOKEN, Instant.now()));
        currencyData2.setDescription("Dummy token for testing");
        currencyData2.setTotalSupply(new BigDecimal("100"));
        currencyData2.setScale(8);
        currencyData2.setCreateTime(Instant.now());

        Hash nativeCurrencyHash = new Hash(7777);
        nativeCurrencyData = new CurrencyData();
        nativeCurrencyData.setHash(nativeCurrencyHash);
        nativeCurrencyData.setName("Coti");
        nativeCurrencyData.setSymbol("COTI");
        currencyService.validateName(nativeCurrencyData);
        currencyService.validateSymbol(nativeCurrencyData);
        nativeCurrencyData.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.NATIVE_COIN, Instant.now()));
        nativeCurrencyData.setDescription("native description");
        nativeCurrencyData.setTotalSupply(new BigDecimal(20000));
        nativeCurrencyData.setScale(8);
        nativeCurrencyData.setCreateTime(Instant.now());
    }

    @Test
    public void getUserTokenGenerationData_UnsignedRequest_shouldReturnBadRequest() {
        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        ResponseEntity<IResponse> expected = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));

        ResponseEntity<IResponse> actual = currencyService.getUserTokens(getUserTokensRequest);
        Assert.assertEquals(expected, actual);

        getUserTokensRequest.setUserHash(new Hash("1111"));
        actual = currencyService.getUserTokens(getUserTokensRequest);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationDataNoUserTokenGenerationsShouldReturnEmptyResponse() {
        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        //getUserTokensRequestCrypto.signMessage(getUserTokensRequest)
        ResponseEntity<IResponse> actual = currencyService.getUserTokens(getUserTokensRequest);
        ResponseEntity<IResponse> expected = ResponseEntity.ok(new GetUserTokensResponse());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationDataOnlyPendingTokenShouldReturnResponseWithAvailableTransaction() {
        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
//        getUserTokensRequestCrypto.signMessage(getUserTokensRequest)
        ResponseEntity<IResponse> actual = currencyService.getUserTokens(getUserTokensRequest);

        GetUserTokensResponse getUserTokensResponse = new GetUserTokensResponse();
//        getTokenGenerationDataResponse.addUnusedConfirmedTransaction(transactionHash)
        ResponseEntity<IResponse> expected = ResponseEntity.ok(getUserTokensResponse);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationDataOnlyCompletedTokenShouldReturnResponseWithAvailableTransactionAndCurrency() {
        when(currencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, currencyData.getHash());

        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
//        getUserTokensRequestCrypto.signMessage(getUserTokensRequest)
        ResponseEntity<IResponse> actual = currencyService.getUserTokens(getUserTokensRequest);

        GetUserTokensResponse getUserTokensResponse = new GetUserTokensResponse();
//        getTokenGenerationDataResponse.addCompletedTransactionHashToGeneratedCurrency(transactionHash, currencyData)
        ResponseEntity<IResponse> expected = ResponseEntity.ok(getUserTokensResponse);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationDataOnlyPendingTokenShouldReturnResponseWithPendingTransactionAndCurrency() {

//        when(pendingCurrencies.getByHash(currencyData.getHash())).thenReturn(currencyData)

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, currencyData.getHash());

        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);
        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
//        getUserTokensRequestCrypto.signMessage(getUserTokensRequest)
        ResponseEntity<IResponse> actual = currencyService.getUserTokens(getUserTokensRequest);

        GetUserTokensResponse getUserTokensResponse = new GetUserTokensResponse();
//        getTokenGenerationDataResponse.addPendingTransactionHashToGeneratedCurrency(transactionHash, currencyData)
        ResponseEntity<IResponse> expected = ResponseEntity.ok(getUserTokensResponse);
        Assert.assertEquals(expected, actual);
    }

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
        Assert.assertEquals(HttpStatus.OK, tokens.getStatusCode());
        Assert.assertEquals(currencyData.getName(), ((GetCurrenciesResponse) tokens.getBody()).getTokens().get(0).getName());
        Assert.assertEquals(currencyData2.getName(), ((GetCurrenciesResponse) tokens.getBody()).getTokens().get(1).getName());
    }
}
