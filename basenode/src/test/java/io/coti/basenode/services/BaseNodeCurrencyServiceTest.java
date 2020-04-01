package io.coti.basenode.services;


import io.coti.basenode.crypto.*;
import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.CurrencyTypeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.model.Currencies;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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
import testUtils.BaseNodeTestUtils;

import java.time.Instant;
import java.util.HashSet;

import static testUtils.CurrencyServiceTestUtils.createCurrencyData;


@ContextConfiguration(classes = {BaseNodeCurrencyService.class, BaseNodeRocksDBConnector.class,
        Currencies.class, NodeCryptoHelper.class,
        BaseNodeNetworkService.class, RestTemplate.class, CurrencyRegistrarCrypto.class,
        ApplicationContext.class,
        GetUpdatedCurrencyRequestCrypto.class, GetUpdatedCurrencyResponseCrypto.class,
        CurrencyTypeRegistrationCrypto.class
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
    private CurrencyRegistrarCrypto currencyRegistrarCrypto;
    @Autowired
    private CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @Autowired
    private GetUpdatedCurrencyResponseCrypto getUpdatedCurrencyResponseCrypto;

    @MockBean
    private Currencies currencies;
    @MockBean
    private BaseNodeNetworkService baseNodeNetworkService;

    @Before
    public void init() {
    }


    @Test
    public void getNativeCurrencyData_addNativeCurrencyIfNeeded_verifyCurrencyExists() {


        final CurrencyData nativeCurrencyData = baseNodeCurrencyService.getNativeCurrency();
        if (nativeCurrencyData != null) {
            Assert.assertEquals(nativeCurrencyData.getCurrencyTypeData().getCurrencyType(), CurrencyType.NATIVE_COIN);
        } else {
            HashSet nativeCurrencyHashes = baseNodeCurrencyService.getCurrencyHashesByCurrencyType(CurrencyType.NATIVE_COIN);
            Assert.assertTrue(nativeCurrencyHashes == null || nativeCurrencyHashes.isEmpty());
            CurrencyType currencyType = CurrencyType.NATIVE_COIN;
            Hash currencyHash = BaseNodeTestUtils.generateRandomHash(136);
            CurrencyData currencyData = createCurrencyData("Coti Test name", "Coti Test Symbol", currencyHash);
            setAndSignCurrencyDataByType(currencyData, currencyType);
            baseNodeCurrencyService.putCurrencyData(currencyData);
            Assert.assertEquals(baseNodeCurrencyService.getNativeCurrency(), currencyData);
        }
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

    protected void setAndSignCurrencyDataByType(CurrencyData currencyData, CurrencyType currencyType) {
        CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now());
//        currencyTypeCrypto.signMessage(currencyTypeData);
//        currencyTypeRegistrationCrypto.signMessage(currencyTypeData);
        currencyData.setCurrencyTypeData(currencyTypeData);
        currencyRegistrarCrypto.signMessage(currencyData);
    }

}
