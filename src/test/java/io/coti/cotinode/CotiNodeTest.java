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
import org.junit.BeforeClass;
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

    private final static String signatureMessage = "message";


    @BeforeClass
    public static void init() {
        BalanceServiceTests.deleteRocksDBfolder();
    }

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
        addTransactionRequest.message = signatureMessage;

        ResponseEntity<AddTransactionResponse> responseEntity = transactionService.addNewTransaction(addTransactionRequest);
        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.CREATED));


        ConfirmationData unconfirmedData = unconfirmedTransactions.getByHash(new Hash("A1"));

        Assert.assertNull(unconfirmedData);

        ConfirmationData confirmedData = confirmedTransactions.getByHash(new Hash("A1"));

        Assert.assertNotNull(confirmedData);


    }

    private List<BaseTransactionData> createBaseTransactionList(int numOfBaseTransactions){
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        for(int i = 0 ; i < numOfBaseTransactions ; i++){
            privatekeyInt++;
        BigInteger privateKey = BigInteger.valueOf(privatekeyInt);
        BaseTransactionData baseTransactionData =
                new BaseTransactionData(new Hash(CryptoUtils.getPublicKeyFromPrivateKey(privateKey).toByteArray()),
                        getRandomDouble(),new Hash(getRandomHexa()),
                        CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(privateKey, signatureMessage));

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
