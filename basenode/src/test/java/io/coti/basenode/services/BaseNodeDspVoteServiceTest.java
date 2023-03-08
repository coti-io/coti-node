package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.utils.HashTestUtils;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {BaseNodeDspVoteService.class})

@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeDspVoteServiceTest {

    @Autowired
    private BaseNodeDspVoteService baseNodeDspVoteService;
    @MockBean
    protected IConfirmationService confirmationServiceLocal;
    @MockBean
    protected IPropagationPublisher propagationPublisherLocal;
    @MockBean
    private Transactions transactionsLocal;
    @MockBean
    private TransactionIndexes transactionIndexesLocal;
    @MockBean
    BaseNodeTransactionHelper transactionHelper;
    @MockBean
    public INetworkService networkService;
    private final Map<Hash, TransactionIndexData> transactionIndexesMap = new HashMap<>();

    @BeforeEach
    void init() {
        baseNodeDspVoteService.init();
        nodeTransactionHelper = transactionHelper;
        confirmationService = confirmationServiceLocal;
        transactions = transactionsLocal;
        propagationPublisher = propagationPublisherLocal;
        transactionIndexes = transactionIndexesLocal;
    }

    @Test
    void handleDspConsensusResultResend_no_data_for_index(CapturedOutput output) {
        NodeResendDcrData nodeResendDcrData = new NodeResendDcrData(HashTestUtils.generateRandomHash(), NodeType.FullNode, 1L, 1L);
        baseNodeDspVoteService.handleDspConsensusResultResend(nodeResendDcrData);
        Assertions.assertTrue(output.getOut().contains("Error, there is no TransactionIndexData for index:"));
    }

    @Test
    void handleDspConsensusResultResend_no_dcr_for_transaction(CapturedOutput output) {
        Hash transactionHash = HashTestUtils.generateRandomHash();
        NodeResendDcrData nodeResendDcrData = new NodeResendDcrData(transactionHash, NodeType.FullNode, 1L, 1L);
        TransactionIndexData transactionIndexData = new TransactionIndexData(transactionHash, 1L, "1".getBytes());
        transactionIndexesMap.put(new Hash(1L), transactionIndexData);
        when(transactionIndexes.getByHash(any(Hash.class))).then((a -> transactionIndexesMap.get(a.getArgument(0))));
        baseNodeDspVoteService.handleDspConsensusResultResend(nodeResendDcrData);
        Assertions.assertTrue(output.getOut().contains("Error, there is no DSP Consensus Result for transaction:"));
    }

    @Test
    void handleDspConsensusResultResend() {
        Hash transactionHash = HashTestUtils.generateRandomHash();
        NodeResendDcrData nodeResendDcrData = new NodeResendDcrData(transactionHash, NodeType.FullNode, 1L, 1L);
        TransactionIndexData transactionIndexData = new TransactionIndexData(transactionHash, 1L, "1".getBytes(StandardCharsets.UTF_8));
        transactionIndexesMap.put(new Hash(1L), transactionIndexData);
        when(transactionIndexes.getByHash(any(Hash.class))).then(a -> transactionIndexesMap.get(a.getArgument(0)));
        TransactionData transaction = TransactionTestUtils.createRandomTransaction();
        transaction.setDspConsensusResult(new DspConsensusResult(transaction.getHash()));
        when(transactions.getByHash(any(Hash.class))).then(a -> transaction);
        baseNodeDspVoteService.handleDspConsensusResultResend(nodeResendDcrData);
        verify(propagationPublisher, atLeastOnce()).propagate(transaction.getDspConsensusResult(), Collections.singletonList(nodeResendDcrData.getNodeType()));
    }
}