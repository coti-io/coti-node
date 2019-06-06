package io.coti.fullnode.services;


import io.coti.basenode.communication.JacksonSerializer;
import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.fullnode.crypto.FullNodeFeeRequestCrypto;
import io.coti.fullnode.http.FullNodeFeeRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_TRUST_SCORE_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {FeeService.class, NodeCryptoHelper.class,
        Transactions.class, BaseNodeTransactionService.class})
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class FeeServiceTest {

    @Autowired
    private FeeService feeService;

    @Autowired
    private FullNodeFeeRequestCrypto fullNodeFeeRequestCrypto;

    @MockBean
    private NodeCryptoHelper nodeCryptoHelper;
    @MockBean
    IDatabaseConnector iDatabaseConnector;
    @MockBean
    private TransactionHelper transactionHelper;
    @MockBean
    private IValidationService validationService;

    @MockBean
    private INetworkService mockINetworkService;

    @MockBean
    private FullNodeFeeRequestCrypto mockFullNodeFeeRequestCrypto;
    @MockBean
    private TransactionIndexService mockTransactionIndexService;
    @MockBean
    private JacksonSerializer jacksonSerializer;
    @MockBean
    private TransactionIndexes mockTransactionIndexes;
    @MockBean
    private BaseTransactionCrypto mockBaseTransactionCrypto;
    @MockBean
    private BaseTransactionData mockBaseTransactionData;




    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createFullNodeFee() {
        FullNodeFeeRequest fullNodeFeeRequest = new FullNodeFeeRequest();
        @Positive BigDecimal originalAmount = TestUtils.generateRandomPositiveBigDecimal();
        fullNodeFeeRequest.setOriginalAmount(originalAmount);
        fullNodeFeeRequest.setUserHash(TestUtils.generateRandomHash());



        Hash hash = TestUtils.generateRandomHash();


        ResponseEntity<BaseResponse> fullNodeFeeResponse = feeService.createFullNodeFee(fullNodeFeeRequest);
        Assert.assertEquals(INVALID_SIGNATURE, ((Response)fullNodeFeeResponse.getBody()).getMessage());

        when(fullNodeFeeRequestCrypto.verifySignature(any(FullNodeFeeRequest.class))).thenReturn(true);


        boolean failed = false;
        try{
            fullNodeFeeResponse = feeService.createFullNodeFee(fullNodeFeeRequest);
        } catch (RuntimeException e) {
            failed = true;
        }
        Assert.assertTrue(failed);

        //TODO:

        FeeService feeServiceSpy = Mockito.spy(new FeeService());
        doReturn(hash).when(feeServiceSpy).getAddress();    // Nullify needed properties

    }


}
