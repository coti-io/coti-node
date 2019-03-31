package io.coti.basenode.services;


import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static testUtils.BaseNodeTestUtils.generateRandomDspConsensusResult;

@ContextConfiguration(classes = {
        BaseNodeDspVoteService.class}
)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class BaseNodeDspVoteServiceTest {

    @Autowired
    private BaseNodeDspVoteService baseNodeDspVoteService;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private IConfirmationService confirmationService;
    @MockBean
    private IPropagationPublisher propagationPublisher;

    @Before
    public void setUp() {
        log.info("Starting  - " + this.getClass().getSimpleName());
    }

    @Test
    public void handleVoteConclusion() {
        DspConsensusResult dspConsensusResult = generateRandomDspConsensusResult();

        // When DspConsensus is invalid do not update DSPC
        baseNodeDspVoteService.handleVoteConclusion(dspConsensusResult);
        Mockito.verify(confirmationService, Mockito.times(0)).setDspcToTrue(any(DspConsensusResult.class));

        // When DspConsensus is valid update DSPC
        when(transactionHelper.handleVoteConclusionResult(any(DspConsensusResult.class))).thenReturn(true);
        baseNodeDspVoteService.handleVoteConclusion(dspConsensusResult);
        Mockito.verify(confirmationService, Mockito.times(1)).setDspcToTrue(any(DspConsensusResult.class));
    }

}
