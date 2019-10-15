package io.coti.zerospend.services;

import io.coti.basenode.communication.ZeroMQPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.*;
import io.coti.basenode.data.*;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IChunkService;
import org.junit.Assert;
import org.junit.Before;
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

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_SUCCESS;
import static org.mockito.Mockito.when;
import static utils.TransactionTestUtils.generateRandomHash;

@ContextConfiguration(classes = {CurrencyService.class,
        CurrencyOriginatorCrypto.class, CurrencyRegistrarCrypto.class, ClusterStampService.class,
        ZeroMQPropagationPublisher.class, NetworkService.class
})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class CurrencyServiceTest {

    private static final String CURRENCY_NAME = "AlexToken";
    public static final String CURRENCY_SYMBOL = "ALX";
    public static final String CURRENCY_DESCRIPTION = "Dummy token for testing";
    public static final String TOTAL_SUPPLY = "100";

    @Autowired
    private CurrencyService currencyService;
    @MockBean
    private CurrencyOriginatorCrypto currencyOriginatorCrypto;
    @MockBean
    private CurrencyRegistrarCrypto currencyRegistrarCrypto;
    @MockBean
    private ClusterStampService clusterStampService;
    @MockBean
    private IPropagationPublisher propagationPublisher;
    @MockBean
    private NetworkService networkService;

    @MockBean
    protected Currencies currencies;
    @MockBean
    protected RestTemplate restTemplate;
    @MockBean
    protected ApplicationContext applicationContext;
    @MockBean
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @MockBean
    protected CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @MockBean
    private HttpJacksonSerializer jacksonSerializer;
    @MockBean
    private IChunkService chunkService;
    @MockBean
    protected IBalanceService balanceService;

    private Hash userHash;
    private Hash currencyHash;
    private OriginatorCurrencyData originatorCurrencyData;
    private CurrencyData currencyData;

    @Before
    public void setUpBeforeEachTest() {

        userHash = generateRandomHash(4);
        userHash = new Hash("1234");
        currencyHash = generateRandomHash(8);

        originatorCurrencyData = new OriginatorCurrencyData();
        originatorCurrencyData.setName(CURRENCY_NAME);
        originatorCurrencyData.setSymbol(CURRENCY_SYMBOL);
        originatorCurrencyData.setDescription(CURRENCY_DESCRIPTION);
        originatorCurrencyData.setTotalSupply(new BigDecimal(TOTAL_SUPPLY));
        originatorCurrencyData.setScale(8);
        originatorCurrencyData.setOriginatorHash(userHash);

        currencyData = new CurrencyData();
        currencyData.setOriginatorHash(originatorCurrencyData.getOriginatorHash());
        currencyData.setHash(currencyHash);
        currencyData.setName(CURRENCY_NAME);
        currencyData.setSymbol(CURRENCY_SYMBOL);
        currencyData.setCurrencyTypeData(new CurrencyTypeData(CurrencyType.PAYMENT_CMD_TOKEN, Instant.now()));
        currencyData.setDescription(CURRENCY_DESCRIPTION);
        currencyData.setTotalSupply(new BigDecimal(TOTAL_SUPPLY));
        currencyData.setScale(8);
        currencyData.setCreationTime(Instant.now());
        currencyData.setRegistrarHash(generateRandomHash(8));

    }

    protected void currencyServiceInit() {
        when(currencies.isEmpty()).thenReturn(Boolean.TRUE);
        currencyService.init();
    }


    @Test
    public void initiateToken_originatorCryptoFails_authorizationFailure() {
        currencyServiceInit();

        when(currencyOriginatorCrypto.verifySignature(currencyData)).thenReturn(false);
        when(currencyRegistrarCrypto.verifySignature(currencyData)).thenReturn(true);
        NetworkNodeData financialServerNodeData = new NetworkNodeData();
        financialServerNodeData.setSignerHash(currencyData.getRegistrarHash());
        when(networkService.getSingleNodeData(NodeType.FinancialServer)).thenReturn(financialServerNodeData);
        when(currencies.getByHash(currencyData.getHash())).thenReturn(null);

        String clusterTimeStamp = String.valueOf(Instant.now().toEpochMilli());
        ClusterStampNameData clusterStampData = new ClusterStampNameData(ClusterStampType.TOKEN, clusterTimeStamp, clusterTimeStamp);
        when(clusterStampService.handleNewToken(currencyData)).thenReturn(clusterStampData);

        ResponseEntity<IResponse> responseEntity = currencyService.initiateToken(currencyData);

        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        Assert.assertTrue(((Response)responseEntity.getBody()).getStatus().equals(STATUS_ERROR));
    }

    @Test
    public void initiateToken_registrarCryptoFails_authorizationFailure() {
        currencyServiceInit();

        when(currencyOriginatorCrypto.verifySignature(currencyData)).thenReturn(true);
        when(currencyRegistrarCrypto.verifySignature(currencyData)).thenReturn(false);
        NetworkNodeData financialServerNodeData = new NetworkNodeData();
        financialServerNodeData.setSignerHash(currencyData.getRegistrarHash());
        when(networkService.getSingleNodeData(NodeType.FinancialServer)).thenReturn(financialServerNodeData);
        when(currencies.getByHash(currencyData.getHash())).thenReturn(null);

        String clusterTimeStamp = String.valueOf(Instant.now().toEpochMilli());
        ClusterStampNameData clusterStampData = new ClusterStampNameData(ClusterStampType.TOKEN, clusterTimeStamp, clusterTimeStamp);
        when(clusterStampService.handleNewToken(currencyData)).thenReturn(clusterStampData);

        ResponseEntity<IResponse> responseEntity = currencyService.initiateToken(currencyData);

        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        Assert.assertTrue(((Response)responseEntity.getBody()).getStatus().equals(STATUS_ERROR));
    }

    @Test
    public void initiateToken_nonFinancialNode_authorizationFailure() {
        currencyServiceInit();

        when(currencyOriginatorCrypto.verifySignature(currencyData)).thenReturn(true);
        when(currencyRegistrarCrypto.verifySignature(currencyData)).thenReturn(true);
        NetworkNodeData financialServerNodeData = new NetworkNodeData();
        financialServerNodeData.setSignerHash(generateRandomHash(4));
        when(networkService.getSingleNodeData(NodeType.FinancialServer)).thenReturn(financialServerNodeData);
        when(currencies.getByHash(currencyData.getHash())).thenReturn(null);

        String clusterTimeStamp = String.valueOf(Instant.now().toEpochMilli());
        ClusterStampNameData clusterStampData = new ClusterStampNameData(ClusterStampType.TOKEN, clusterTimeStamp, clusterTimeStamp);
        when(clusterStampService.handleNewToken(currencyData)).thenReturn(clusterStampData);

        ResponseEntity<IResponse> responseEntity = currencyService.initiateToken(currencyData);

        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        Assert.assertTrue(((Response)responseEntity.getBody()).getStatus().equals(STATUS_ERROR));
    }

    @Test
    public void initiateToken_preexistingCurrency_badRequestFailure() {
        currencyServiceInit();

        when(currencyOriginatorCrypto.verifySignature(currencyData)).thenReturn(true);
        when(currencyRegistrarCrypto.verifySignature(currencyData)).thenReturn(true);
        NetworkNodeData financialServerNodeData = new NetworkNodeData();
        financialServerNodeData.setSignerHash(currencyData.getRegistrarHash());
        when(networkService.getSingleNodeData(NodeType.FinancialServer)).thenReturn(financialServerNodeData);
        when(currencies.getByHash(currencyData.getHash())).thenReturn(currencyData);

        String clusterTimeStamp = String.valueOf(Instant.now().toEpochMilli());
        ClusterStampNameData clusterStampData = new ClusterStampNameData(ClusterStampType.TOKEN, clusterTimeStamp, clusterTimeStamp);
        when(clusterStampService.handleNewToken(currencyData)).thenReturn(clusterStampData);

        ResponseEntity<IResponse> responseEntity = currencyService.initiateToken(currencyData);

        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        Assert.assertTrue(((Response)responseEntity.getBody()).getStatus().equals(STATUS_ERROR));
    }

    @Test
    public void initiateToken_nativeCurrency_badRequestFailure() {
        currencyServiceInit();

        when(currencyOriginatorCrypto.verifySignature(currencyData)).thenReturn(true);
        when(currencyRegistrarCrypto.verifySignature(currencyData)).thenReturn(true);
        NetworkNodeData financialServerNodeData = new NetworkNodeData();
        financialServerNodeData.setSignerHash(currencyData.getRegistrarHash());
        when(networkService.getSingleNodeData(NodeType.FinancialServer)).thenReturn(financialServerNodeData);
        when(currencies.getByHash(currencyData.getHash())).thenReturn(null);
        currencyData.getCurrencyTypeData().setCurrencyType(CurrencyType.NATIVE_COIN);

        String clusterTimeStamp = String.valueOf(Instant.now().toEpochMilli());
        ClusterStampNameData clusterStampData = new ClusterStampNameData(ClusterStampType.TOKEN, clusterTimeStamp, clusterTimeStamp);
        when(clusterStampService.handleNewToken(currencyData)).thenReturn(clusterStampData);

        ResponseEntity<IResponse> responseEntity = currencyService.initiateToken(currencyData);

        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.BAD_REQUEST));
        Assert.assertTrue(((Response)responseEntity.getBody()).getStatus().equals(STATUS_ERROR));
    }



    @Test
    public void initiateToken_success() {
        currencyServiceInit();

        when(currencyOriginatorCrypto.verifySignature(currencyData)).thenReturn(true);
        when(currencyRegistrarCrypto.verifySignature(currencyData)).thenReturn(true);
        NetworkNodeData financialServerNodeData = new NetworkNodeData();
        financialServerNodeData.setSignerHash(currencyData.getRegistrarHash());
        when(networkService.getSingleNodeData(NodeType.FinancialServer)).thenReturn(financialServerNodeData);
        when(currencies.getByHash(currencyData.getHash())).thenReturn(null);

        String clusterTimeStamp = String.valueOf(Instant.now().toEpochMilli());
        ClusterStampNameData clusterStampData = new ClusterStampNameData(ClusterStampType.TOKEN, clusterTimeStamp, clusterTimeStamp);
        when(clusterStampService.handleNewToken(currencyData)).thenReturn(clusterStampData);

        ResponseEntity<IResponse> responseEntity = currencyService.initiateToken(currencyData);

        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));
        Assert.assertTrue(((Response)responseEntity.getBody()).getStatus().equals(STATUS_SUCCESS));
    }

}
