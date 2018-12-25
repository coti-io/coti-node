package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.config.WebShutDown;
import io.coti.basenode.controllers.TransactionBatchController;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static testUtils.TestUtils.generateRandomHash;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = DspVoteService.class)
@RunWith(SpringRunner.class)
@Slf4j
public class DspVoteServiceTest {
    private static final int SIZE_OF_HASH = 64;

    @MockBean
    protected ITransactionHelper transactionHelper;
    @MockBean
    protected IConfirmationService confirmationService;
    @MockBean
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    private DspVoteService dspVoteService;

    @Test
    public void continueHandleVoteConclusion() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        dspVoteService.continueHandleVoteConclusion(new DspConsensusResult( generateRandomHash(SIZE_OF_HASH)));
    }
}