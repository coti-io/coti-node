package integrationTest;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

//@ContextConfiguration(classes = IntegrationServiceTestsAppConfig.class)
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "../test.properties")
@Slf4j

public class PropagationServiceTest {

    @Test
    public void propagateToNeighbors() {
    }

    @Test
    public void getTransactionFromNeighbors() {
    }

}