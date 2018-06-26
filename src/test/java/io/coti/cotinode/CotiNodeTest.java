package io.coti.cotinode;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.model.ConfirmedTransactions;
import io.coti.cotinode.model.UnconfirmedTransactions;
import io.coti.cotinode.service.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;
import java.util.List;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
public class CotiNodeTest {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private ConfirmedTransactions confirmedTransactions;

    @Autowired
    private UnconfirmedTransactions unconfirmedTransactions;

    /*
    scenario:
    Transaction{
        base transaction AE,100.1
        base transaction BE,100.1
        base transaction CE,100.1
    }
    Addresses doesn't exist in balance / pre balance maps



     */

    @Test
    public void testFullProcess(){

        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        BaseTransactionData baseTransactionData = new BaseTransactionData("AE",100.1);
        baseTransactionData.setSignature("1C$80ECA0013536B8446714934649665E93C78B4B35184E9832F43D18C8F00411D4$670A77A6892681C1EFC5F6F08FA92590D44AC6C2536AAA3E481408F5BD18AA88");
        BaseTransactionData baseTransactionData2 = new BaseTransactionData("BE",200.1);
        baseTransactionData2.setSignature("1C$80ECA0013536B8446714934649665E93C78B4B35184E9832F43D18C8F00411D4$670A77A6892681C1EFC5F6F08FA92590D44AC6C2536AAA3E481408F5BD18AA88");
        BaseTransactionData baseTransactionData3 = new BaseTransactionData("CE",300.1);
        baseTransactionData3.setSignature("1C$80ECA0013536B8446714934649665E93C78B4B35184E9832F43D18C8F00411D4$670A77A6892681C1EFC5F6F08FA92590D44AC6C2536AAA3E481408F5BD18AA88");
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.add(baseTransactionData);
        baseTransactionDataList.add(baseTransactionData2);
        baseTransactionDataList.add(baseTransactionData3);

        addTransactionRequest.baseTransactions = baseTransactionDataList;
        addTransactionRequest.transactionHash = new Hash("A1");
        addTransactionRequest.message = "some message";

        ResponseEntity<AddTransactionResponse> responseEntity =  transactionService.addNewTransaction(addTransactionRequest);
        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.OK));



        ConfirmationData confirmationData = confirmedTransactions.getByHash(new Hash("A1"));

        Assert.assertNotNull(confirmationData);





    }
}
