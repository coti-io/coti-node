package io.coti.zerospend.services;

import io.coti.basenode.services.interfaces.IClusterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

import static testUtils.TestUtils.generateRandomLongNumber;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = SourceStarvationService.class)
@Slf4j
public class SourceStarvationServiceTest {

    @Autowired
    private SourceStarvationService sourceStarvationService;

    @MockBean
    private IClusterService clusterService;
    @MockBean
    private TransactionCreationService transactionCreationService;

    @Test
    public void checkSourcesStarvation_noExceptionIsThrown() {
        try {
            sourceStarvationService.checkSourcesStarvation();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void millisecondsToMinutes() {
        long milliseconds = generateRandomLongNumber();

        String millisecondsToMinutes = sourceStarvationService.millisecondsToMinutes(milliseconds);

        Assert.assertEquals(millisecondsToMinutes, new SimpleDateFormat("mm:ss").format(new Date(milliseconds)));
    }
}