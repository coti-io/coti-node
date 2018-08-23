package integrationTest;

import io.coti.common.services.InitializationService;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.MonitorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@ContextConfiguration(classes = IntegrationServiceTestsAppConfig.class)
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "../test.properties")
@Slf4j

public class MonitorServiceTest {
    @Autowired
    private InitializationService initializationService;

    @Autowired
    private MonitorService monitorService;

    @MockBean
    private WebSocketSender webSocketSender;

    @Test
    public void lastState() {

        monitorService.lastState();
    }

}
