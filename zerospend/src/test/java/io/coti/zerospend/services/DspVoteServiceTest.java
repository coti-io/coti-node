package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.DspVoteCrypto;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionVotes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.ICommunicationService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.LinkedList;

import static org.mockito.Mockito.when;

import static testUtils.TestUtils.*;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = {DspVoteService.class, BaseNodeNetworkService.class})
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
    @Autowired
    private INetworkService networkService;
    @MockBean
    private ICommunicationService communicationService;
    @MockBean
    private NetworkNodeCrypto networkNodeCrypto;
    @MockBean
    private NodeRegistrationCrypto nodeRegistrationCrypto;


    @Before
    public void init() {
        dspVoteService.init();
    }

    @Test
    public void preparePropagatedTransactionForVoting() {
        try {
            TransactionData txData = createRandomTransaction();
            TransactionVoteData txVoteData = new TransactionVoteData(txData.getHash(), new LinkedList<>());
            networkService.init();
            dspVoteService.preparePropagatedTransactionForVoting(txData);
            Mockito.verify(transactionVotes, Mockito.times(1)).put(txVoteData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void receiveDspVote() {

        DspVote dspVote = TestUtils.generateRandomDspVote();

        // When Tx does not exist
        when(transactionVotes.getByHash(dspVote.getHash())).thenReturn(null);
        String result = dspVoteService.receiveDspVote(dspVote);
        Assert.assertEquals("Transaction does not exist", result);

        // When Tx vote exists but is matching hash is not marked as legal
        TransactionVoteData txVoteDataUnauthorized = TestUtils.generateTxVoteDataByDspVote(dspVote);
        txVoteDataUnauthorized.getLegalVoterDspHashes().remove(dspVote.getVoterDspHash());
        when(transactionVotes.getByHash(dspVote.getHash())).thenReturn(txVoteDataUnauthorized);
        result = dspVoteService.receiveDspVote(dspVote);
        Assert.assertEquals("Unauthorized", result);

        // When Tx vote exists and hash is legal, but signature check is failed
        TransactionVoteData txVoteData = TestUtils.generateTxVoteDataByDspVote(dspVote);
        when(transactionVotes.getByHash(dspVote.getHash())).thenReturn(txVoteData);
        when(dspVoteCrypto.verifySignature(dspVote)).thenReturn(false);
        result = dspVoteService.receiveDspVote(dspVote);
        Assert.assertEquals("Invalid Signature", result);

        // When vote was already processed and is no longer on waiting
        txVoteData = TestUtils.generateTxVoteDataByDspVote(dspVote);
        when(transactionVotes.getByHash(dspVote.getHash())).thenReturn(txVoteData);
        when(dspVoteCrypto.verifySignature(dspVote)).thenReturn(true);
        result = dspVoteService.receiveDspVote(dspVote);
        Assert.assertEquals("Vote already processed", result);

        // When vote is valid and waiting
        TransactionData txData = TestUtils.generateRandomTxData();
        txData.setHash(dspVote.getTransactionHash());
        networkService.init();
        dspVoteService.preparePropagatedTransactionForVoting(txData);
        result = dspVoteService.receiveDspVote(dspVote);
        Assert.assertEquals("Ok", result);
    }


    @Test
    public void setIndexForDspResult() {
        try {
            TransactionIndexData transactionIndexData = TestUtils.generateTransactionIndexData();
            TransactionData txData = createRandomTransaction();
            when(transactionIndexService.getLastTransactionIndexData()).thenReturn(transactionIndexData);
            when(transactionIndexService.insertNewTransactionIndex(txData)).thenReturn(true);
            dspVoteService.setIndexForDspResult(txData, new DspConsensusResult(TestUtils.generateRandomHash()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

 }