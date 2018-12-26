package io.coti.zerospend.services;

import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeConfirmationService;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static testUtils.TestUtils.generateRandomTransaction;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ConfirmationService.class)
@Slf4j
public class ConfirmationServiceTest {
    @Autowired
    private ConfirmationService confirmationService;

    @MockBean
    private LiveViewService liveViewService;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private Transactions transactions;

    @Test
    public void insertNewTransactionIndex() {
        Assert.assertTrue(confirmationService.insertNewTransactionIndex(generateRandomTransaction()));
    }
}