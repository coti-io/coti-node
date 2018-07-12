package io.coti.fullnode.service;

import io.coti.fullnode.controllers.PropagationController;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = io.coti.fullnode.AppConfig.class)
@Slf4j
public class PropagationServiceTest {
    @Autowired
    private io.coti.fullnode.service.PropagationService propagationService;

    @Autowired
    private PropagationController propagationController;

    @Before
    public void setUp () throws Exception {

    }

    @Test
    public void propagateToNeighbors () {

    }

    @Test
    public void propagateFromNeighbors () {

    }


    @Test
    public void getTransactionFromCurrentNode () {
    }

    @Test
    public void loadNodesList () {
    }

    @Test
    public void loadCurrentNode () {
    }
}