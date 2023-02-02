package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeIdentityService;
import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.financialserver.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

@ContextConfiguration(classes = {TransactionCryptoCreator.class, TransactionCrypto.class, BaseNodeIdentityService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class TransactionCryptoCreatorTest {

    @Autowired
    private TransactionCryptoCreator transactionCryptoCreator;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private BaseNodeIdentityService nodeIdentityService;
    private static final String SEED = "6c233873925b0cbf352bd5cb4f5548cb4f5c5e2e33664043f6f31c883304b6ce";

    @Test
    void test_sign_base_transactions() {
        BaseNodeServiceManager.nodeIdentityService = nodeIdentityService;

        Hash address = TransactionTestUtils.generateRandomAddressHash();
        nodeIdentityService.setSeed(SEED);
        TransactionData transactionData = TransactionTestUtils.createRandomTransactionWithAddress(address);
        Map<Hash, Integer> addressHashToAddressIndexMap = new HashMap<>();
        addressHashToAddressIndexMap.put(address, 1);
        transactionCryptoCreator.signBaseTransactions(transactionData, addressHashToAddressIndexMap);
        Assertions.assertNotEquals(null, transactionData.getInputBaseTransactions().get(0).getSignatureData());
    }
}