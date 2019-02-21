package io.coti.basenode.services;

import io.coti.basenode.crypto.ClusterStampStateCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.ClusterStampPreparationData;
import io.coti.basenode.data.ClusterStampState;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


import static org.junit.Assert.*;

//TODO 2/19/2019 astolia: TEST isn't working yet
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ValidationService.class, ClusterStampStateCrypto.class})
@WebMvcTest
public class ValidationServiceTest {


    @Autowired
    private IValidationService validationService;

    @Autowired
    private ClusterStampStateCrypto clusterStampStateCrypto;

    //
    @MockBean
    private Transactions transactions;
    @MockBean
    private ITransactionHelper transactionHelper;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private IPotService potService;
    @MockBean
    private NodeCryptoHelper codeCryptoHelper;
    //
//
//
//
//
    @Test
    public void validatePrepareForClusterStampRequest() {

//        ClusterStampPreparationData clusterStampPreparationDataSigned = new ClusterStampPreparationData(1l);
//        clusterStampStateCrypto.signMessage(clusterStampPreparationDataSigned);
//
//        ClusterStampPreparationData clusterStampPreparationDataUnsigned = new ClusterStampPreparationData(1l);
//
//        assertTrue(validationService.validatePrepareForClusterStampRequest(clusterStampPreparationDataSigned, ClusterStampState.OFF));
//        assertFalse(validationService.validatePrepareForClusterStampRequest(clusterStampPreparationDataUnsigned, ClusterStampState.OFF));
//        assertFalse(validationService.validatePrepareForClusterStampRequest(clusterStampPreparationDataUnsigned, ClusterStampState.PREPARING));
//        assertFalse(validationService.validatePrepareForClusterStampRequest(clusterStampPreparationDataUnsigned, ClusterStampState.IN_PROCESS));
    }
}