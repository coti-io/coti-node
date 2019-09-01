package io.coti.basenode.services;


import io.coti.basenode.crypto.*;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.model.CurrencySymbolIndexes;
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

@ContextConfiguration(classes = {BaseNodeCurrencyService.class, BaseNodeRocksDBConnector.class,
        Currencies.class, NodeCryptoHelper.class,
        BaseNodeNetworkService.class, RestTemplate.class, CurrencyCrypto.class,
        CurrencyNameIndexes.class, CurrencySymbolIndexes.class, ApplicationContext.class,
        GetUpdatedCurrencyRequestCrypto.class, GetUpdatedCurrencyResponseCrypto.class, CurrencyTypeCrypto.class


})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeCurrencyServiceTest {

    @Autowired
    private BaseNodeCurrencyService baseNodeCurrencyService;
    @Autowired
    private BaseNodeRocksDBConnector baseNodeRocksDBConnector;
    @Autowired
    public IDatabaseConnector databaseConnector;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CurrencyCrypto currencyCrypto;
    @Autowired
    private CurrencyNameIndexes currencyNameIndexes;
    @Autowired
    private CurrencySymbolIndexes currencySymbolIndexes;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @Autowired
    private GetUpdatedCurrencyResponseCrypto getUpdatedCurrencyResponseCrypto;
    @Autowired
    private CurrencyTypeCrypto currencyTypeCrypto;

    @MockBean
    private Currencies currencies;
    @MockBean
    private BaseNodeNetworkService baseNodeNetworkService;

    @Before
    public void init() {
    }

    @Test
    public void getMissingCurrencies_noPreexistingCurrencies_noReturnedCurrencies() {
//        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
//        Set<Hash> missingCurrencyHashes = new HashSet<>();
//        getCurrencyRequest.setCurrenciesHashes(missingCurrencyHashes);
//
//        Hash signerHash = new Hash("PublicKey");
//        getCurrencyRequest.setSignerHash(signerHash);
//        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);

//        ResponseEntity<BaseResponse> missingCurrencies = baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
//
//        Assert.assertEquals(missingCurrencies.getStatusCode(), (HttpStatus.OK));
//        Assert.assertTrue(((GetCurrencyResponse) missingCurrencies.getBody()).getCurrencyDataSet().isEmpty());
    }

    @Test
    public void getMissingCurrencies_badSignature_unauthorized() {
//        GetCurrencyRequest getCurrencyRequest = new GetCurrencyRequest();
//        Set<Hash> missingCurrencyHashes = new HashSet<>();
//        getCurrencyRequest.setCurrenciesHashes(missingCurrencyHashes);
//
//        Hash signerHash = new Hash("PublicKey");
//        getCurrencyRequest.setSignerHash(signerHash);
//        getCurrencyRequestCrypto.signMessage(getCurrencyRequest);
//        missingCurrencyHashes.add(BaseNodeTestUtils.generateRandomHash());

//        ResponseEntity<BaseResponse> missingCurrencies = baseNodeCurrencyService.getMissingCurrencies(getCurrencyRequest);
//
//        Assert.assertEquals(missingCurrencies.getStatusCode(), (HttpStatus.UNAUTHORIZED));
//        Assert.assertEquals(missingCurrencies.getBody().getStatus(),STATUS_ERROR);
    }


}
