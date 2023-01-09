package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.pot.PotRunnableTask;
import io.coti.basenode.utils.TransactionTestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.atomic.AtomicInteger;

@ContextConfiguration(classes = {BaseNodePotService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodePotServiceTest {

    @Autowired
    private BaseNodePotService baseNodePotService;

    @Test
    void validatePot() {
        baseNodePotService.init();
        TransactionData transactionData = TransactionTestUtils.createRandomTransaction();
        runPot(transactionData);
        Assertions.assertTrue(baseNodePotService.validatePot(transactionData));
    }

    private void runPot(TransactionData transactionData) {
        final AtomicInteger lock = new AtomicInteger(0);
        PotRunnableTask pot = new PotRunnableTask(transactionData, baseNodePotService.targetDifficulty, lock);
        pot.run();
    }
}