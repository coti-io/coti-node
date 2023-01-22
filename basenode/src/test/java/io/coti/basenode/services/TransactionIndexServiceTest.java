package io.coti.basenode.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionIndexData;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.services.interfaces.INetworkService;
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

import java.time.Instant;
import java.util.Optional;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeTransactionHelper;
import static io.coti.basenode.services.BaseNodeServiceManager.transactionIndexes;

@ContextConfiguration(classes = {TransactionIndexService.class})
@ExtendWith(OutputCaptureExtension.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class TransactionIndexServiceTest {

    @Autowired
    private TransactionIndexService transactionIndexService;
    @MockBean
    private TransactionIndexes transactionIndexesLocal;
    @MockBean
    private BaseNodeTransactionHelper baseNodeTransactionHelper;
    @MockBean
    INetworkService networkService;

    @BeforeEach
    void init() {
        transactionIndexes = transactionIndexesLocal;
        nodeTransactionHelper = baseNodeTransactionHelper;
    }

    @Test
    void insert_new_transaction_index_empty(CapturedOutput output) {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        Assertions.assertEquals(Optional.empty(), transactionIndexService.insertNewTransactionIndex(transactionData));
        Assertions.assertTrue(output.getOut().contains("Invalid transaction index for transaction"));
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndex(0);
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), 0, "GENESIS".getBytes());
        transactionIndexService.setLastTransactionIndexData(transactionIndexData);
        Assertions.assertEquals(Optional.empty(), transactionIndexService.insertNewTransactionIndex(transactionData));
    }

    @Test
    void insert_new_transaction_index_true() {
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setIndex(0);
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusResult.setDspConsensus(true);
        transactionData.setDspConsensusResult(dspConsensusResult);
        TransactionIndexData transactionIndexData = new TransactionIndexData(TransactionTestUtils.generateRandomHash(), -1, "GENESIS".getBytes());
        transactionIndexService.setLastTransactionIndexData(transactionIndexData);
        Assertions.assertEquals(Optional.of(Boolean.TRUE), transactionIndexService.insertNewTransactionIndex(transactionData));
    }
}