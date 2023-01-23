package io.coti.fullnode.services;

import io.coti.basenode.data.TransactionData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import utils.TestUtils;

import java.time.Instant;

@ContextConfiguration(classes = {PotService.class})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
class PotServiceTest {

    @Autowired
    private PotService potService;

    @BeforeEach
    void init() {
        potService.init();
    }

    @Test
    void potAction() {
        TransactionData transactionData = TestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        potService.potAction(transactionData);
        Assertions.assertNotNull(transactionData.getNonces());
    }

    @Test
    void executorSizes() {
        TransactionData transactionData = TestUtils.createRandomTransaction();
        transactionData.setAttachmentTime(Instant.now());
        transactionData.setSenderTrustScore(10);
        potService.potAction(transactionData);
        Assertions.assertFalse(potService.executorSizes(10).isEmpty());
    }
}