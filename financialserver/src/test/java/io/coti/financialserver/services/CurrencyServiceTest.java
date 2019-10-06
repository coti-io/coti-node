package io.coti.financialserver.services;

import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.*;
import io.coti.basenode.data.*;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.*;
import io.coti.financialserver.crypto.GenerateTokenRequestCrypto;
import io.coti.financialserver.crypto.GetUserTokensRequestCrypto;
import io.coti.financialserver.crypto.ReceiverBaseTransactionOwnerCrypto;
import io.coti.financialserver.http.GenerateTokenRequest;
import io.coti.financialserver.http.GetTokenGenerationDataResponse;
import io.coti.financialserver.http.GetUserTokensRequest;
import io.coti.financialserver.http.data.GeneratedTokenResponseData;
import io.coti.financialserver.model.CurrencyNameIndexes;
import io.coti.financialserver.model.PendingCurrencies;
import io.coti.financialserver.model.ReceiverBaseTransactionOwners;
import io.coti.financialserver.model.UserTokenGenerations;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static io.coti.financialserver.http.HttpStringConstants.TOKEN_GENERATION_REQUEST_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static utils.HashTestUtils.generateRandomHash;
import static utils.TransactionTestUtils.createRandomTransaction;

@Slf4j
@ContextConfiguration(classes = {CurrencyService.class, GetUserTokensRequestCrypto.class, NodeCryptoHelper.class, UserTokenGenerations.class,
        TransactionService.class, ConfirmationService.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class CurrencyServiceTest {

    private static final String CURRENCY_NAME = "AlexToken";
    public static final String CURRENCY_SYMBOL = "ALX";
    public static final String CURRENCY_DESCRIPTION = "Dummy token for testing";
    public static final String TOTAL_SUPPLY = "100";
    public static final String RECOVER_NATIVE_CURRENCY_PATH = "null/currencies/native";
    @Autowired
    private CurrencyService currencyService;
    @MockBean
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @MockBean
    private UserTokenGenerations userTokenGenerations;
    @MockBean
    private PendingCurrencies pendingCurrencies;
    @MockBean
    private Currencies currencies;

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
    @MockBean
    private TransactionHelper transactionHelper;

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

    @Autowired
    private TransactionService transactionService;
    @MockBean
    private ReceiverBaseTransactionOwnerCrypto receiverBaseTransactionOwnerCrypto;
    @MockBean
    private RollingReserveService rollingReserveService;
    @MockBean
    private ReceiverBaseTransactionOwners receiverBaseTransactionOwners;
    @MockBean
    private IValidationService validationService;
    @MockBean
    private IDspVoteService dspVoteService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private JacksonSerializer jacksonSerializer2;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private IClusterStampService clusterStampService;
    @MockBean
    private IDatabaseConnector databaseConnector;

    @Autowired
    private ConfirmationService confirmationService;
    @MockBean
    private IBalanceService balanceService;

    private Hash userHash;
    private Hash extraUserHash;
    private Hash currencyHash;
    private Hash nativeCurrencyHash;
    private Hash transactionHash;
    private Hash extraTransactionHash;
    private OriginatorCurrencyData originatorCurrencyData;
    private OriginatorCurrencyData extraOriginatorCurrencyData;
    private CurrencyData currencyData;
    private CurrencyData nativeCurrencyData;
    private TransactionData transactionData;
    private TransactionData extraTransactionData;


    @Before
    public void setUpBeforeEachTest() {
        Mockito.reset(userTokenGenerations, pendingCurrencies, currencies,
                getUserTokensRequestCrypto, currencyOriginatorCrypto,
                generateTokenRequestCrypto, transactionHelper, transactions);

//        userHash = generateRandomHash(4);
        userHash = new Hash("1234");
        extraUserHash = generateRandomHash(8);
        transactionHash = generateRandomHash(8);
        extraTransactionHash = generateRandomHash(8);
        currencyHash = generateRandomHash(8);
        nativeCurrencyHash = generateRandomHash(8);

        originatorCurrencyData = new OriginatorCurrencyData();
        originatorCurrencyData.setName(CURRENCY_NAME);
        originatorCurrencyData.setSymbol(CURRENCY_SYMBOL);
        originatorCurrencyData.setDescription(CURRENCY_DESCRIPTION);
        originatorCurrencyData.setTotalSupply(new BigDecimal(TOTAL_SUPPLY));
        originatorCurrencyData.setScale(8);
        originatorCurrencyData.setOriginatorHash(userHash);

        extraOriginatorCurrencyData = new OriginatorCurrencyData();
        extraOriginatorCurrencyData.setName(CURRENCY_NAME);
        extraOriginatorCurrencyData.setSymbol(CURRENCY_SYMBOL);
        extraOriginatorCurrencyData.setDescription(CURRENCY_DESCRIPTION);
        extraOriginatorCurrencyData.setTotalSupply(new BigDecimal(TOTAL_SUPPLY));
        extraOriginatorCurrencyData.setScale(8);
        extraOriginatorCurrencyData.setOriginatorHash(new Hash("1234"));

        currencyData = new CurrencyData();
        currencyData.setHash(currencyHash);
        currencyData.setName(CURRENCY_NAME);
        currencyData.setSymbol(CURRENCY_SYMBOL);
        currencyData.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.PAYMENT_CMD_TOKEN, Instant.now()));
        currencyData.setDescription(CURRENCY_DESCRIPTION);
        currencyData.setTotalSupply(new BigDecimal(TOTAL_SUPPLY));
        currencyData.setScale(8);
        currencyData.setCreationTime(Instant.now());

        nativeCurrencyData = new CurrencyData();
        nativeCurrencyData.setHash(nativeCurrencyHash);
        nativeCurrencyData.setName("Coti");
        nativeCurrencyData.setSymbol("COTI");
        nativeCurrencyData.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.NATIVE_COIN, Instant.now()));
        nativeCurrencyData.setDescription(CURRENCY_DESCRIPTION);
        nativeCurrencyData.setTotalSupply(new BigDecimal(TOTAL_SUPPLY));
        nativeCurrencyData.setScale(8);
        nativeCurrencyData.setCreationTime(Instant.now());

        transactionData = createRandomTransaction();
        transactionData.setType(TransactionType.TokenGeneration);
        transactionData.setSenderHash(userHash);
        transactionData.setHash(transactionHash);

        extraTransactionData = createRandomTransaction();
        extraTransactionData.setType(TransactionType.TokenGeneration);
//        extraTransactionData.setSenderHash(extraUserHash);
        extraTransactionData.setSenderHash(new Hash("1234"));
        extraTransactionData.setHash(extraTransactionHash);
    }

    protected void currencyServiceInit() {
        when(currencies.isEmpty()).thenReturn(Boolean.TRUE);
        when(restTemplate.getForObject(RECOVER_NATIVE_CURRENCY_PATH, CurrencyData.class)).thenReturn(nativeCurrencyData);
        currencyService.init();
    }

    @Test
    public void getUserTokenGenerationData_UnsignedRequest_shouldReturnBadRequest() {
        currencyServiceInit();

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        ResponseEntity expected = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(null, STATUS_ERROR));

        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);

        Assert.assertEquals(expected.getStatusCode(), actual.getStatusCode());
    }

    @Test
    public void getUserTokenGenerationData_noUserTokenGenerations_shouldReturnEmptyResponse() {
        currencyServiceInit();

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        when(getUserTokensRequestCrypto.verifySignature(getUserTokensRequest)).thenReturn(Boolean.TRUE);

        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);

        ResponseEntity expected = ResponseEntity.ok(new GetTokenGenerationDataResponse());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getUserTokenGenerationData_onlyPendingTransaction_shouldReturnResponseWithAvailableTransaction() {
        currencyServiceInit();

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, null);
        final GeneratedTokenResponseData pendingGeneratedToken = new GeneratedTokenResponseData(transactionHash, null, false);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);

        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);
        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);
        when(getUserTokensRequestCrypto.verifySignature(getUserTokensRequest)).thenReturn(Boolean.TRUE);

        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);

        Assert.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assert.assertTrue(((GetTokenGenerationDataResponse) actual.getBody()).getGeneratedTokens().contains(pendingGeneratedToken));
    }

    @Test
    public void getUserTokenGenerationData_onlyPendingToken_shouldReturnResponseWithAvailableTransactionAndCurrency() {
        currencyServiceInit();

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, currencyData.getHash());
        final GeneratedTokenResponseData pendingGeneratedToken = new GeneratedTokenResponseData(transactionHash, currencyData, false);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);

        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);
        when(getUserTokensRequestCrypto.verifySignature(getUserTokensRequest)).thenReturn(Boolean.TRUE);
        when(pendingCurrencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);

        Assert.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assert.assertTrue(((GetTokenGenerationDataResponse) actual.getBody()).getGeneratedTokens().contains(pendingGeneratedToken));
    }

    @Test
    public void getUserTokenGenerationData_onlyCompletedToken_shouldReturnResponseWithAvailableTransactionAndCurrency() {
        currencyServiceInit();

        Map<Hash, Hash> transactionHashToCurrencyHash = new HashMap<>();
        transactionHashToCurrencyHash.put(transactionHash, currencyData.getHash());
        final GeneratedTokenResponseData completedGeneratedToken = new GeneratedTokenResponseData(transactionHash, currencyData, true);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyHash);

        GetUserTokensRequest getUserTokensRequest = new GetUserTokensRequest();
        getUserTokensRequest.setUserHash(userHash);

        when(userTokenGenerations.getByHash(userTokenGenerationData.getUserHash())).thenReturn(userTokenGenerationData);
        when(getUserTokensRequestCrypto.verifySignature(getUserTokensRequest)).thenReturn(Boolean.TRUE);
        when(currencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        ResponseEntity actual = currencyService.getUserTokens(getUserTokensRequest);

        Assert.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assert.assertTrue(((GetTokenGenerationDataResponse) actual.getBody()).getGeneratedTokens().contains(completedGeneratedToken));
    }

    @Test
    public void generateToken_noTransactionAttached_requestDenied() {
        currencyServiceInit();

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest();
        generateTokenRequest.setTransactionHash(transactionHash);
        generateTokenRequest.setOriginatorCurrencyData(originatorCurrencyData);

        when(generateTokenRequestCrypto.verifySignature(generateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);

        ResponseEntity<IResponse> responseEntity = currencyService.generateToken(generateTokenRequest);

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        Assert.assertEquals(STATUS_ERROR, ((Response) responseEntity.getBody()).getStatus());
    }

    @Test
    public void generateToken_transactionAttached_currencyPending() {
        currencyServiceInit();

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest();
        generateTokenRequest.setTransactionHash(transactionHash);
        generateTokenRequest.setOriginatorCurrencyData(originatorCurrencyData);
        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
        transactionHashToCurrencyMap.put(transactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyMap);

        when(generateTokenRequestCrypto.verifySignature(generateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        when(transactionHelper.isConfirmed(any())).thenReturn(Boolean.FALSE);
        when(transactions.getByHash(transactionHash)).thenReturn(transactionData);

        ResponseEntity<IResponse> responseEntity = currencyService.generateToken(generateTokenRequest);

        ResponseEntity<Response> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
        Assert.assertEquals(expectedResponse, responseEntity);
    }

    @Test
    public void generateToken_transactionConfirmed_currencyCreated() {
        currencyServiceInit();

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest();
        generateTokenRequest.setTransactionHash(transactionHash);
        generateTokenRequest.setOriginatorCurrencyData(originatorCurrencyData);
        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
        transactionHashToCurrencyMap.put(transactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyMap);

        when(generateTokenRequestCrypto.verifySignature(generateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        when(transactionHelper.isConfirmed(any())).thenReturn(Boolean.TRUE);
        when(transactions.getByHash(transactionHash)).thenReturn(transactionData);

        ResponseEntity<IResponse> responseEntity = currencyService.generateToken(generateTokenRequest);

        ResponseEntity<Response> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
        Assert.assertEquals(expectedResponse, responseEntity);
    }

    @Test
    public void attachTransactionGenerateTokenConfirmTransaction_transactionAttached_currencyPending() {
        currencyServiceInit();

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest();
        generateTokenRequest.setTransactionHash(transactionHash);
        generateTokenRequest.setOriginatorCurrencyData(originatorCurrencyData);
        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
        transactionHashToCurrencyMap.put(transactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyMap);

        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        transactionService.continueHandlePropagatedTransaction(transactionData);

        when(generateTokenRequestCrypto.verifySignature(generateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        when(transactionHelper.isConfirmed(any())).thenReturn(Boolean.FALSE);
        when(transactions.getByHash(transactionHash)).thenReturn(transactionData);

        ResponseEntity<IResponse> responseEntity = currencyService.generateToken(generateTokenRequest);

        ResponseEntity<Response> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
        Assert.assertEquals(expectedResponse, responseEntity);
    }


    @Test
    public void attachTransactionGenerateTokenConfirmTransaction_transactionAttachedGenerateRequestTransactionConfirmed_currencyCreated() {
        currencyServiceInit();

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest();
        generateTokenRequest.setTransactionHash(transactionHash);
        generateTokenRequest.setOriginatorCurrencyData(originatorCurrencyData);
        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
        transactionHashToCurrencyMap.put(transactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyMap);

        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        transactionService.continueHandlePropagatedTransaction(transactionData);

        when(generateTokenRequestCrypto.verifySignature(generateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);
        when(transactionHelper.isConfirmed(any())).thenReturn(Boolean.FALSE);
        when(transactions.getByHash(transactionHash)).thenReturn(transactionData);

        ResponseEntity<IResponse> responseEntity = currencyService.generateToken(generateTokenRequest);

        ResponseEntity<Response> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
        Assert.assertEquals(expectedResponse, responseEntity);

        userTokenGenerationData.getTransactionHashToCurrencyMap().put(transactionHash, currencyHash);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        when(pendingCurrencies.getByHash(any(Hash.class))).thenReturn(currencyData);

        log.info("Starting continueHandleConfirmedTransaction");
        confirmationService.continueHandleConfirmedTransaction(transactionData);
    }

    @Test
    public void attachTransactionsDifferentUsersGenerateToken_transactionAttachedGenerateRequestTransactionConfirmed_currencyCreated() {
        currencyServiceInit();

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest();
        generateTokenRequest.setTransactionHash(transactionHash);
        generateTokenRequest.setOriginatorCurrencyData(originatorCurrencyData);
        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
        transactionHashToCurrencyMap.put(transactionHash, null);
        Map<Hash, Hash> extraTransactionHashToCurrencyMap = new HashMap<>();
        extraTransactionHashToCurrencyMap.put(extraTransactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyMap);
        UserTokenGenerationData extraUserTokenGenerationData = new UserTokenGenerationData(extraUserHash, extraTransactionHashToCurrencyMap);
        extraTransactionData.setSenderHash(extraUserHash);

        when(userTokenGenerations.getByHash(extraUserHash)).thenReturn(extraUserTokenGenerationData);
        transactionService.continueHandlePropagatedTransaction(extraTransactionData);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        transactionService.continueHandlePropagatedTransaction(transactionData);

        when(generateTokenRequestCrypto.verifySignature(generateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        when(transactionHelper.isConfirmed(any())).thenReturn(Boolean.FALSE);
        when(transactions.getByHash(transactionHash)).thenReturn(transactionData);

        ResponseEntity<IResponse> responseEntity = currencyService.generateToken(generateTokenRequest);

        ResponseEntity<Response> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
        Assert.assertEquals(expectedResponse, responseEntity);

        userTokenGenerationData.getTransactionHashToCurrencyMap().put(transactionHash, currencyHash);
        when(userTokenGenerations.getByHash(any(Hash.class))).thenReturn(userTokenGenerationData);
        when(pendingCurrencies.getByHash(any(Hash.class))).thenReturn(currencyData);

        log.info("Starting continueHandleConfirmedTransaction");
        confirmationService.continueHandleConfirmedTransaction(transactionData);
    }

    @Test
    public void attachTransactionsSameUserGenerateTokenConfirmTransaction_transactionAttachedGenerateRequestTransactionConfirmed_currencyCreated() {
        currencyServiceInit();

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest();
        generateTokenRequest.setTransactionHash(transactionHash);
        generateTokenRequest.setOriginatorCurrencyData(originatorCurrencyData);
        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
        transactionHashToCurrencyMap.put(transactionHash, null);
        transactionHashToCurrencyMap.put(extraTransactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyMap);

        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        transactionService.continueHandlePropagatedTransaction(extraTransactionData);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        transactionService.continueHandlePropagatedTransaction(transactionData);

        when(generateTokenRequestCrypto.verifySignature(generateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        when(transactionHelper.isConfirmed(any())).thenReturn(Boolean.FALSE);
        when(transactions.getByHash(transactionHash)).thenReturn(transactionData);

        ResponseEntity<IResponse> responseEntity = currencyService.generateToken(generateTokenRequest);

        ResponseEntity<Response> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
        Assert.assertEquals(expectedResponse, responseEntity);

        when(userTokenGenerations.getByHash(any(Hash.class))).thenReturn(userTokenGenerationData);
        when(pendingCurrencies.getByHash(any(Hash.class))).thenReturn(currencyData);

        log.info("Starting continueHandleConfirmedTransaction");
        confirmationService.continueHandleConfirmedTransaction(transactionData);

    }

    @Test
    public void attachTransactionsSameUserGenerateTokensConfirmTransaction_transactionAttachedGenerateRequestTransactionConfirmed_currencyCreated() {
        currencyServiceInit();

        GenerateTokenRequest generateTokenRequest = new GenerateTokenRequest();
        generateTokenRequest.setTransactionHash(transactionHash);
        generateTokenRequest.setOriginatorCurrencyData(originatorCurrencyData);
        GenerateTokenRequest extraGenerateTokenRequest = new GenerateTokenRequest();
        extraGenerateTokenRequest.setTransactionHash(extraTransactionHash);
        extraGenerateTokenRequest.setOriginatorCurrencyData(extraOriginatorCurrencyData);

        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
        transactionHashToCurrencyMap.put(transactionHash, null);
        transactionHashToCurrencyMap.put(extraTransactionHash, null);
        UserTokenGenerationData userTokenGenerationData = new UserTokenGenerationData(userHash, transactionHashToCurrencyMap);

        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        transactionService.continueHandlePropagatedTransaction(extraTransactionData);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        transactionService.continueHandlePropagatedTransaction(transactionData);

        when(generateTokenRequestCrypto.verifySignature(generateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        when(transactionHelper.isConfirmed(any())).thenReturn(Boolean.FALSE);
        when(transactions.getByHash(transactionHash)).thenReturn(transactionData);

        ResponseEntity<IResponse> responseEntity = currencyService.generateToken(generateTokenRequest);

        when(generateTokenRequestCrypto.verifySignature(extraGenerateTokenRequest)).thenReturn(Boolean.TRUE);
        when(currencyOriginatorCrypto.verifySignature(new CurrencyData(originatorCurrencyData))).thenReturn(Boolean.TRUE);
        when(userTokenGenerations.getByHash(userHash)).thenReturn(userTokenGenerationData);
        when(transactionHelper.isConfirmed(any())).thenReturn(Boolean.FALSE);
        when(transactions.getByHash(extraTransactionHash)).thenReturn(extraTransactionData);

        ResponseEntity<IResponse> responseEntity2 = currencyService.generateToken(extraGenerateTokenRequest);

        ResponseEntity<Response> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
        Assert.assertEquals(expectedResponse, responseEntity);

        when(userTokenGenerations.getByHash(any(Hash.class))).thenReturn(userTokenGenerationData);
        when(pendingCurrencies.getByHash(any(Hash.class))).thenReturn(currencyData);

        log.info("Starting continueHandleConfirmedTransaction");
        confirmationService.continueHandleConfirmedTransaction(transactionData);

    }

}
