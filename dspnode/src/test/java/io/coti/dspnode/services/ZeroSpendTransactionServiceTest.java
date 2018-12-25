package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.ZeroSpendTransactionRequest;
import io.coti.basenode.services.BaseNodeInitializationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(classes = ZeroSpendTransactionService.class)
@RunWith(SpringRunner.class)
@Slf4j
public class ZeroSpendTransactionServiceTest {

    @Autowired
    private ZeroSpendTransactionService zeroSpendTransactionService;

    @MockBean
    private ISender sender;

    @Test
    public void handleReceivedZeroSpendTransactionRequest() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        zeroSpendTransactionService.handleReceivedZeroSpendTransactionRequest(new ZeroSpendTransactionRequest());
    }
}