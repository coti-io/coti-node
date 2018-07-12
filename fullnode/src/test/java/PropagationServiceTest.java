import io.coti.fullnode.AppConfig;
import io.coti.fullnode.service.PropagationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
public class PropagationServiceTest {

    @Autowired
    private PropagationService propagationService;


    @Test
    public void propagateToNeighbors() {
    }

    @Test
    public void getTransactionFromNeighbors() {
    }

}