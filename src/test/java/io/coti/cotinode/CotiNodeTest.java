package io.coti.cotinode;

import io.coti.cotinode.crypto.CryptoUtils;
import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.http.HttpStringConstants;
import io.coti.cotinode.model.ConfirmedTransactions;
import io.coti.cotinode.model.UnconfirmedTransactions;
import io.coti.cotinode.service.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
   This is a good scenario where amount and address are dynamically generated
  */
    @Test
    public void aTestFullProcess(){
        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        List<BaseTransactionData> baseTransactionDataList = createBaseTransactionRandomList(3);

        addTransactionRequest.baseTransactions = baseTransactionDataList;
        addTransactionRequest.transactionHash = new Hash("A1");
        addTransactionRequest.message = signatureMessage;

        ResponseEntity<AddTransactionResponse> responseEntity = transactionService.addNewTransaction(addTransactionRequest);
        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        try {
            log.info("CotiNodeTest is going to sleep for 20 sec");
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ConfirmationData confirmedData = confirmedTransactions.getByHash(new Hash("A1"));

        ConfirmationData unconfirmedData = unconfirmedTransactions.getByHash(new Hash("A1"));

        Assert.assertNotNull(confirmedData);
        Assert.assertNull(unconfirmedData);

    }

 //   @Test
    public void bTestBadScenarioNewTransactionNegativeAmount() {
        String baseTransactionHexaAddress = getRandomHexa();
        double minusAmount = -1.0;
        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress, minusAmount);

        ResponseEntity<AddTransactionResponse> badResponseEntity = transactionService.
                addNewTransaction(createRequestWithOneBaseTransaction(new Hash("AB"),new Hash(baseTransactionHexaAddress),minusAmount));
        Assert.assertTrue(badResponseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        Assert.assertTrue(badResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_ERROR));

    }

 //   @Test
    public void cTestBadScenarioTwoTransactionsSecondNegative(){
        String baseTransactionHexaAddress = getRandomHexa();
        double plusAmount = 50;
        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress, plusAmount);
        ResponseEntity<AddTransactionResponse> goodResponseEntity = transactionService.
                addNewTransaction(createRequestWithOneBaseTransaction(new Hash("AC")
                        ,new Hash(baseTransactionHexaAddress),plusAmount));
        Assert.assertTrue(goodResponseEntity.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(goodResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        double minusAmount = -80;
        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress, minusAmount);
        ResponseEntity<AddTransactionResponse> badResponseEntity = transactionService.
                addNewTransaction(createRequestWithOneBaseTransaction(new Hash("ACC")
                        ,new Hash(baseTransactionHexaAddress),minusAmount));
        Assert.assertTrue(badResponseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        Assert.assertTrue(badResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_ERROR));


    }


    private AddTransactionRequest createRequestWithOneBaseTransaction(Hash transactionHash , Hash baseTransactionAddress , Double amount){
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();

        BaseTransactionData baseTransactionData =
                new BaseTransactionData(new Hash(CryptoUtils.getPublicKeyFromPrivateKey(BigInteger.valueOf(123)).toByteArray()),
                        amount, baseTransactionAddress,
                        CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(BigInteger.valueOf(123), signatureMessage));
        baseTransactionDataList.add(baseTransactionData);

        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        addTransactionRequest.baseTransactions = baseTransactionDataList;
        addTransactionRequest.transactionHash = transactionHash;
        addTransactionRequest.message = signatureMessage;
        return addTransactionRequest;
    }


    private List<BaseTransactionData> createBaseTransactionRandomList(int numOfBaseTransactions){
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
