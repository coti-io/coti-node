package io.coti.basenode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.exceptions.ClusterStampException;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {BaseNodeClusterStampService.class})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
class BaseNodeClusterStampServiceTest {

    @Autowired
    private BaseNodeClusterStampService baseNodeClusterStampService;

    @MockBean
    protected IBalanceService balanceService;
    @MockBean
    protected TrustChainConfirmationService trustChainConfirmationService;
    @MockBean
    protected Transactions transactions;
    @MockBean
    protected ClusterStampCrypto clusterStampCrypto;
    @MockBean
    protected INetworkService networkService;
    @MockBean
    protected ICurrencyService currencyService;

    @Test
    void init() {
        Assertions.assertThrows(ClusterStampException.class, () -> baseNodeClusterStampService.init());
    }
}
