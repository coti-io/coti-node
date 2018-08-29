package integrationTest;

import io.coti.common.crypto.DspConsensusCrypto;
import io.coti.common.crypto.TransactionTrustScoreCrypto;
import io.coti.common.database.RocksDBConnector;
import io.coti.common.model.*;
import io.coti.common.services.*;
import io.coti.common.services.LiveView.LiveViewService;
import io.coti.common.services.LiveView.WebSocketSender;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan(
        basePackageClasses = InitializationService.class,
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE, value = {Transactions.class,
                        Addresses.class,
                        RocksDBConnector.class,
                        AddressesTransactionsHistory.class,
                        TrustScores.class,
                        TransactionIndexes.class,
                        TransactionVotes.class,
                        InitializationService.class,
                        TransactionIndexService.class,
                        BalanceService.class,
                        WebSocketSender.class,
                        LiveViewService.class,
                        TransactionHelper.class,
                        ClusterService.class,
                        SourceSelector.class,
                        TccConfirmationService.class,
                        DspConsensusCrypto.class,
                        TransactionTrustScoreCrypto.class,
                        MonitorService.class,
                        WebSocketSender.class} )
        }
)
@EnableScheduling
@PropertySource("classpath:application.properties")
public class IntegrationServiceTestsAppConfig {

}
