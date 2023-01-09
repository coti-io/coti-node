package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.data.HealthMetricData;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.utilities.MonitorConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL;
import static io.coti.basenode.constants.BaseNodeHealthMetricConstants.TOTAL_TRANSACTIONS_LABEL;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {BaseNodeMonitorService.class, BaseNodeServiceManager.class,
        MonitorConfigurationProperties.class})
@EnableConfigurationProperties(value = MonitorConfigurationProperties.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
public class BaseNodeMonitorServiceTest {

    @Autowired
    protected IMonitorService monitorService;
    @Autowired
    private BaseNodeServiceManager baseNodeServiceManager;
    @Autowired
    private MonitorConfigurationProperties monitorConfigurationProperties;
    @MockBean
    protected INetworkService networkService;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private IConfirmationService confirmationService;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private TrustChainConfirmationService trustChainConfirmationService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private ITransactionService transactionService;
    @MockBean
    private IPropagationSubscriber propagationSubscriber;
    @MockBean
    private IWebSocketMessageService webSocketMessageService;
    @MockBean
    private IReceiver receiver;
    @MockBean
    private IPropagationPublisher propagationPublisher;
    @MockBean
    private IDatabaseConnector databaseConnector;
    @MockBean
    private IDBRecoveryService dbRecoveryService;

    @BeforeEach
    public void init() {
        monitorService.initNodeMonitor();
    }

    private void initMetricData(HealthMetric healthMetric) {
        HealthMetricData healthMetricDataInitial = monitorService.getHealthMetricData(healthMetric);
        healthMetricDataInitial.setMetricValue(0);
        healthMetricDataInitial.setPreviousMetricValue(0);
        healthMetricDataInitial.setDegradingCounter(0);
        healthMetricDataInitial.setLastHealthState(BaseNodeMonitorService.HealthState.NA);
        healthMetricDataInitial.getAdditionalValues().clear();
    }

    @Test
    public void totalTransactionsMetric_checkCondition_normal() {
        long totalTransactionsFromLocal = 7;
        long totalTransactionsFromRecovery = 7;
        HealthMetric healthMetric = HealthMetric.TOTAL_TRANSACTIONS_DELTA;
        initMetricData(healthMetric);

        BaseNodeMonitorService.HealthState healthState = BaseNodeMonitorService.HealthState.NORMAL;
        totalTransactionsCheckHealthState(totalTransactionsFromLocal, totalTransactionsFromRecovery, healthState, healthMetric);
    }

    @Test
    public void totalTransactionsMetric_checkCondition_warning() {
        long totalTransactionsFromLocal = 7;
        long totalTransactionsFromRecovery = 9;
        HealthMetric healthMetric = HealthMetric.TOTAL_TRANSACTIONS_DELTA;
        initMetricData(healthMetric);
        HealthMetricData healthMetricData = monitorService.getHealthMetricData(healthMetric);
        healthMetricData.increaseDegradingCounter();

        BaseNodeMonitorService.HealthState healthState = BaseNodeMonitorService.HealthState.WARNING;
        totalTransactionsCheckHealthState(totalTransactionsFromLocal, totalTransactionsFromRecovery, healthState, healthMetric);
    }

    @Test
    public void totalTransactionsMetric_checkCondition_critical() {
        long totalTransactionsFromLocal = 7;
        long totalTransactionsFromRecovery = 15;
        HealthMetric healthMetric = HealthMetric.TOTAL_TRANSACTIONS_DELTA;
        initMetricData(healthMetric);
        HealthMetricData healthMetricData = monitorService.getHealthMetricData(healthMetric);
        healthMetricData.setDegradingCounter(3);

        BaseNodeMonitorService.HealthState healthState = BaseNodeMonitorService.HealthState.CRITICAL;
        totalTransactionsCheckHealthState(totalTransactionsFromLocal, totalTransactionsFromRecovery, healthState, healthMetric);
    }

    private void totalTransactionsCheckHealthState(long totalTransactionsFromLocal, long totalTransactionsFromRecovery, BaseNodeMonitorService.HealthState healthState, HealthMetric healthMetric) {
        when(transactionHelper.getTotalTransactions()).thenReturn(totalTransactionsFromLocal);
        when(transactionHelper.getTotalNumberOfTransactionsFromRecovery()).thenReturn(totalTransactionsFromRecovery);

        healthMetric.doSnapshot();
        HealthMetricData healthMetricDataUpdated = monitorService.getHealthMetricData(healthMetric);

        Assertions.assertEquals(totalTransactionsFromLocal, healthMetricDataUpdated.getSpecificLastMetricValue(TOTAL_TRANSACTIONS_LABEL).longValue());
        Assertions.assertEquals(totalTransactionsFromRecovery, healthMetricDataUpdated.getSpecificLastMetricValue(TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL).longValue());

        healthMetric.calculateHealthMetric();

        healthMetricDataUpdated = monitorService.getHealthMetricData(healthMetric);

        Assertions.assertEquals(totalTransactionsFromLocal, healthMetricDataUpdated.getSpecificLastMetricValue(TOTAL_TRANSACTIONS_LABEL).longValue());
        Assertions.assertEquals(totalTransactionsFromRecovery, healthMetricDataUpdated.getSpecificLastMetricValue(TOTAL_TRANSACTIONS_FROM_RECOVERY_LABEL).longValue());
        Assertions.assertEquals(totalTransactionsFromRecovery - totalTransactionsFromLocal, healthMetricDataUpdated.getMetricValue());

        Assertions.assertEquals(healthState, healthMetricDataUpdated.getLastHealthState());
    }

    @Test
    public void percentage_used_heap_memory_normal() {
        HealthMetric healthMetric = HealthMetric.PERCENTAGE_USED_HEAP_MEMORY;
        initMetricData(healthMetric);

        BaseNodeMonitorService.HealthState healthState = BaseNodeMonitorService.HealthState.NORMAL;
        healthMetric.doSnapshot();
        healthMetric.calculateHealthMetric();
        Assertions.assertEquals(healthState, monitorService.getHealthMetricData(healthMetric).getLastHealthState());
    }

    @Test
    public void percentage_used_memory_normal() {
        HealthMetric healthMetric = HealthMetric.PERCENTAGE_USED_MEMORY;
        initMetricData(healthMetric);

        BaseNodeMonitorService.HealthState healthState = BaseNodeMonitorService.HealthState.NORMAL;
        healthMetric.doSnapshot();
        healthMetric.calculateHealthMetric();
        Assertions.assertEquals(healthState, monitorService.getHealthMetricData(healthMetric).getLastHealthState());
    }
}