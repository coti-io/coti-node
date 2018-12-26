package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.DspVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionVotes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static testUtils.TestUtils.*;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = DspVoteService.class)
@RunWith(SpringRunner.class)
@Slf4j
public class DspVoteServiceTest {

    @MockBean
    protected ITransactionHelper transactionHelper;
    @MockBean
    protected IConfirmationService confirmationService;
    @Autowired
    DspVoteService dspVoteService;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private IPropagationPublisher propagationPublisher;
    @MockBean
    private TransactionVotes transactionVotes;
    @MockBean
    private Transactions transactions;
    @MockBean
    private DspVoteCrypto dspVoteCrypto;
    @MockBean
    private DspConsensusCrypto dspConsensusCrypto;

    @Before
    public void init() {
        dspVoteService.init();
    }

    @Test
    public void preparePropagatedTransactionForVoting_noExceptionIsThrown() {
        try {
            dspVoteService.preparePropagatedTransactionForVoting(generateRandomTransaction());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void receiveDspVote() {
        Hash transactionHash = generateRandomHash();
        Hash voterDspHash = generateRandomHash();
        DspVote dspVote = new DspVote(transactionHash, true);
        dspVote.setVoterDspHash(voterDspHash);
        when(transactionVotes.getByHash(transactionHash))
                .thenReturn(new TransactionVoteData(transactionHash, (
                        Collections.singletonList(voterDspHash))));
        when(dspVoteCrypto.verifySignature(dspVote)).thenReturn(true);

        String result = dspVoteService.receiveDspVote(dspVote);

        Assert.assertTrue(result.equals("Vote already processed"));
    }

    @Test
    public void setIndexForDspResult_noExceptionIsThrown() {
        try {
            TransactionIndexData transactionIndexData
                    = new TransactionIndexData(generateRandomHash(), generateRandomLongNumber(), generateRandomHash().getBytes());
            when(transactionIndexService.getLastTransactionIndexData()).thenReturn(transactionIndexData);
            dspVoteService.setIndexForDspResult(generateRandomTransaction(), new DspConsensusResult(generateRandomHash()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}