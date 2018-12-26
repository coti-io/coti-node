package io.coti.zerospend.services;

import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = InitializationService.class)
@Slf4j
public class InitializationServiceTest {
    @Autowired
    private  InitializationService  initializationService;

    @MockBean
    private CommunicationService communicationService;
    @MockBean
    private DspVoteService dspVoteService;
    @MockBean
    private BaseNodeInitializationService baseNodeInitializationService;
    @MockBean
    private TransactionCreationService transactionCreationService;
    @MockBean
    private Transactions transactions;

    @Test
    public void init() {
        // Auto init
    }
}