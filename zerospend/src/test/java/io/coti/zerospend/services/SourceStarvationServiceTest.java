package io.coti.zerospend.services;

import io.coti.basenode.services.interfaces.IClusterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {SourceStarvationService.class})
@Slf4j
public class SourceStarvationServiceTest {

    @MockBean
    IClusterService clusterService;

    @Test
    public void createZeroSpendTest() {

    }

}
