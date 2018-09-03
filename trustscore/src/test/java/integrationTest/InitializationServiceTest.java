package integrationTest;

import io.coti.trustscore.AppConfig;
import io.coti.trustscore.services.InitializationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@SpringBootTest
public class InitializationServiceTest {
    @Autowired
    private InitializationService initializationService;

    @Test
    public void test() {

    }
}