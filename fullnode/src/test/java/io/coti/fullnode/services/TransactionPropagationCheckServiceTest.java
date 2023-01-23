package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.model.UnconfirmedReceivedTransactionHashes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import utils.TestUtils;

import java.util.HashSet;
import java.util.Set;

import static io.coti.basenode.services.BaseNodeServiceManager.unconfirmedReceivedTransactionHashes;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

@ContextConfiguration(classes = {TransactionPropagationCheckService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
class TransactionPropagationCheckServiceTest {

    @Autowired
    private TransactionPropagationCheckService transactionPropagationCheckService;
    @MockBean
    UnconfirmedReceivedTransactionHashes unconfirmedReceivedTransactionHashesLocal;

    @BeforeEach
    void init() {
        unconfirmedReceivedTransactionHashes = unconfirmedReceivedTransactionHashesLocal;
        transactionPropagationCheckService.init();
    }

    @Test
    void testAddNewUnconfirmedTransaction() {
        Hash transactionHash = TestUtils.generateRandomHash();
        Set<UnconfirmedReceivedTransactionHashData> unconfirmedReceivedTransactionHashesSet = new HashSet<>();
        doAnswer(invocation -> {
            Object arg0 = invocation.getArgument(0);
            unconfirmedReceivedTransactionHashesSet.add((UnconfirmedReceivedTransactionHashData) arg0);
            return null;
        }).when(unconfirmedReceivedTransactionHashes).put(any(UnconfirmedReceivedTransactionHashData.class));
        transactionPropagationCheckService.addNewUnconfirmedTransaction(transactionHash);
        Assertions.assertFalse(unconfirmedReceivedTransactionHashesSet.isEmpty());
        Assertions.assertEquals(transactionHash, unconfirmedReceivedTransactionHashesSet.stream().findFirst().get().getTransactionHash());
    }

}

