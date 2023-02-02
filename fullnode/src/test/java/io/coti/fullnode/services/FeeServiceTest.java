package io.coti.fullnode.services;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeIdentityService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.fullnode.crypto.FullNodeFeeRequestCrypto;
import io.coti.fullnode.http.FullNodeFeeRequest;
import io.coti.fullnode.http.FullNodeFeeResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import utils.TestUtils;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.fullnode.services.NodeServiceManager.fullNodeFeeRequestCrypto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {FeeService.class, ValidationService.class, BaseNodeIdentityService.class})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class FeeServiceTest {

    @Autowired
    ValidationService validationServiceLocal;
    @MockBean
    ICurrencyService currencyServiceLocal;
    @MockBean
    FullNodeFeeRequestCrypto fullNodeFeeRequestCryptoLocal;
    @Autowired
    private FeeService feeService;
    @Autowired
    private BaseNodeIdentityService nodeIdentityServiceLocal;

    @BeforeEach
    void init() {
        nodeIdentityService = nodeIdentityServiceLocal;
        currencyService = currencyServiceLocal;
        fullNodeFeeRequestCrypto = fullNodeFeeRequestCryptoLocal;
        validationService = validationServiceLocal;
    }

    @Test
    void createFullNodeFee_multi_dag_is_not_supported() {
        FullNodeFeeRequest fullNodeFeeRequest = TestUtils.createFullNodeFeeRequest();
        ResponseEntity<IResponse> fullNodeFeeResponse = feeService.createFullNodeFee(fullNodeFeeRequest);
        Assertions.assertEquals(STATUS_ERROR, ((Response) fullNodeFeeResponse.getBody()).getStatus());
        Assertions.assertEquals(MULTI_DAG_IS_NOT_SUPPORTED, ((Response) fullNodeFeeResponse.getBody()).getMessage());
    }

    @Test
    void createFullNodeFee_invalid_signature() {
        FullNodeFeeRequest fullNodeFeeRequest = TestUtils.createFullNodeFeeRequest();
        when(currencyService.isCurrencyHashAllowed(any(Hash.class))).thenReturn(true);
        ResponseEntity<IResponse> fullNodeFeeResponse = feeService.createFullNodeFee(fullNodeFeeRequest);
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, fullNodeFeeResponse.getStatusCode());
        Assertions.assertEquals(INVALID_SIGNATURE, ((Response) fullNodeFeeResponse.getBody()).getMessage());
    }

    @Test
    void createFullNodeFee_success() {
        FullNodeFeeRequest fullNodeFeeRequest = TestUtils.createFullNodeFeeRequest();
        when(currencyService.isCurrencyHashAllowed(any(Hash.class))).thenReturn(true);
        when(fullNodeFeeRequestCrypto.verifySignature(fullNodeFeeRequest)).thenReturn(true);
        when(currencyService.isNativeCurrency(any(Hash.class))).thenReturn(true);
        when(currencyService.getNativeCurrencyHash()).thenReturn(OriginatorCurrencyCrypto.calculateHash("COTI"));
        ResponseEntity<IResponse> fullNodeFeeResponse = feeService.createFullNodeFee(fullNodeFeeRequest);
        Assertions.assertEquals(HttpStatus.CREATED, fullNodeFeeResponse.getStatusCode());
        Assertions.assertNotNull(((FullNodeFeeResponse) fullNodeFeeResponse.getBody()).getFullNodeFee().getSignatureData());
    }

    @Test
    void getAddress() {
        Assertions.assertNotNull(feeService.getAddress());
    }

    @Test
    void setFullNodeFeeHash() {
        Hash hash = TestUtils.generateRandomHash();
        FullNodeFeeData fullNodeFeeData = TestUtils.generateFullNodeFeeData(hash, 10);
        feeService.setFullNodeFeeHash(fullNodeFeeData);
        Assertions.assertNotNull(fullNodeFeeData.getHash());
    }
}