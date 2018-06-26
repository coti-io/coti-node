package io.coti.cotinode;

import io.coti.cotinode.crypto.CryptoUtils;
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

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


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

    private String[] hexaOptions = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};

    private int privatekeyInt = 122;


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
        List<BaseTransactionData> baseTransactionDataList = createBaseTransactionList(3);

        addTransactionRequest.baseTransactions = baseTransactionDataList;
        addTransactionRequest.transactionHash = new Hash("A1");
        addTransactionRequest.message = "message";

        ResponseEntity<AddTransactionResponse> responseEntity =  transactionService.addNewTransaction(addTransactionRequest);
        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.CREATED));

        ConfirmationData confirmationData = unconfirmedTransactions.getByHash(new Hash("A1"));

        Assert.assertNotNull(confirmationData);


        /*
        BaseTransactionData baseTransactionData = new BaseTransactionData("00A598A8030DA6D86C6BC7F2F5144EA549D28211EA58FAA70EBF4C1E665C1FE9B5204B5D6F84822C307E4B4A7140737AEC23FC63B65B35F86A10026DBD2D864E6B",100.1);
        baseTransactionData.setSignature("1C$80ECA0013536B8446714934649665E93C78B4B35184E9832F43D18C8F00411D4$670A77A6892681C1EFC5F6F08FA92590D44AC6C2536AAA3E481408F5BD18AA88");
        BaseTransactionData baseTransactionData2 = new BaseTransactionData("00A598A8030DA6D86C6BC7F2F5144EA549D28211EA58FAA70EBF4C1E665C1FE9B5204B5D6F84822C307E4B4A7140737AEC23FC63B65B35F86A10026DBD2D864E6B",200.1);
        baseTransactionData2.setSignature("1C$80ECA0013536B8446714934649665E93C78B4B35184E9832F43D18C8F00411D4$670A77A6892681C1EFC5F6F08FA92590D44AC6C2536AAA3E481408F5BD18AA88");
        BaseTransactionData baseTransactionData3 = new BaseTransactionData("00A598A8030DA6D86C6BC7F2F5144EA549D28211EA58FAA70EBF4C1E665C1FE9B5204B5D6F84822C307E4B4A7140737AEC23FC63B65B35F86A10026DBD2D864E6B",300.1);
        baseTransactionData3.setSignature("1C$80ECA0013536B8446714934649665E93C78B4B35184E9832F43D18C8F00411D4$670A77A6892681C1EFC5F6F08FA92590D44AC6C2536AAA3E481408F5BD18AA88");
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.add(baseTransactionData);
        baseTransactionDataList.add(baseTransactionData2);
        baseTransactionDataList.add(baseTransactionData3);
        */
    }

    private List<BaseTransactionData> createBaseTransactionList(int numOfBaseTransactions){
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        for(int i = 0 ; i < numOfBaseTransactions ; i++){
            privatekeyInt++;
        BigInteger privateKey = new BigInteger(String.valueOf(privatekeyInt));
            BaseTransactionData baseTransactionData =
                    new BaseTransactionData(new Hash(CryptoUtils.getPublicKeyFromPrivateKey(privateKey).toByteArray()),
                            getRandomDouble(),new Hash(getRandomHexa()),
                            CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(privateKey, "message"));
            baseTransactionDataList.add(baseTransactionData);
        }


        return baseTransactionDataList;
    }

    private String getRandomHexa(){
        String hexa = "";
        for(int i =0 ; i < 20 ; i++){
        int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa += hexaOptions[randomNum];
        }
        return hexa;
    }

    private Double getRandomDouble() {
        Random r = new Random();
        return 1 + (100 - 1) * r.nextDouble();
    }
}
