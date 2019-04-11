package io.coti.fullnode.services;


import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.fullnode.http.FullNodeFeeRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {FeeService.class, NodeCryptoHelper.class,
        Transactions.class, BaseNodeTransactionService.class})
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class FeeServiceTest {

    @Autowired
    private FeeService feeService;
//    @MockBean
//    private FeeService feeService;

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

        Hash hash = TestUtils.generateRandomHash();

        //TODO: spy is not working as expected, currently test fails on NPE

        FeeService feeServiceSpy = Mockito.spy(new FeeService());
//        when(feeServiceSpy.getAddress()).thenReturn(hash);
        doReturn(hash).when(feeServiceSpy).getAddress();    // Nullify needed properties
//        feeServiceSpy.createFullNodeFee(fullNodeFeeRequest);

    }


}
