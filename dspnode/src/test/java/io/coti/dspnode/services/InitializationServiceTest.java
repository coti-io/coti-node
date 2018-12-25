package io.coti.dspnode.services;


import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.BaseNodeMonitorService;
import io.coti.basenode.services.CommunicationService;
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
@ContextConfiguration(classes = InitializationService.class)
//@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class InitializationServiceTest {
    @MockBean
    private BaseNodeMonitorService baseNodeMonitorService;
    @MockBean
    private BaseNodeInitializationService baseNodeInitializationService;
    @MockBean
    private TransactionService transactionService;
    @MockBean
    private AddressService addressService;
    @MockBean
    private CommunicationService communicationService;

    @Autowired
    private InitializationService initializationService;

    @Test
    public void init() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        // Auto initialize
    }
}