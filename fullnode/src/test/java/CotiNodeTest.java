import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.common.crypto.CryptoUtils;
import io.coti.common.data.*;
import io.coti.common.http.AddTransactionRequest;
import io.coti.common.http.HttpStringConstants;
import io.coti.common.http.Response;
import io.coti.common.model.ConfirmedTransactions;
import io.coti.common.model.UnconfirmedTransactions;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IPropagationService;
import io.coti.common.services.interfaces.ITransactionService;
import io.coti.fullnode.AppConfig;
import io.coti.fullnode.controllers.TransactionController;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
@TestPropertySource(properties = "resetDatabase=true")
public class CotiNodeTest {


    private final static String transactionDescription = "message";
    private final static SignatureData signatureMessage = new SignatureData("message", "message");
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private TransactionController transactionController;
    @Autowired
    private ConfirmedTransactions confirmedTransactions;
    @Autowired
    private UnconfirmedTransactions unconfirmedTransactions;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IPropagationService propagationService;
    private int privatekeyInt = 122;
    /*
       This is a good scenario where amount and address are dynamically generated
      */


    @Test
    public void aTestFullProcess(){

        TransactionData tx = createTransactionsDataFromJSON(0);
        AddTransactionRequest addTransactionRequest0 = new AddTransactionRequest();
        addTransactionRequest0.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest0.hash = tx.getHash();
        addTransactionRequest0.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest0.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity0 = transactionService.addNewTransaction(addTransactionRequest0);
        Assert.assertTrue(responseEntity0.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity0.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        tx = createTransactionsDataFromJSON(1);
        AddTransactionRequest addTransactionRequest1 = new AddTransactionRequest();
        addTransactionRequest1.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest1.hash = tx.getHash();
        addTransactionRequest1.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest1.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity1 = transactionService.addNewTransaction(addTransactionRequest1);
        Assert.assertTrue(responseEntity1.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity1.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        tx = createTransactionsDataFromJSON(2);
        AddTransactionRequest addTransactionRequest2 = new AddTransactionRequest();
        addTransactionRequest2.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest2.hash = tx.getHash();
        addTransactionRequest2.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest2.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity2 = transactionService.addNewTransaction(addTransactionRequest2);
        Assert.assertTrue(responseEntity2.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity2.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));


        tx = createTransactionsDataFromJSON(3);
        AddTransactionRequest addTransactionRequest3 = new AddTransactionRequest();
        addTransactionRequest3.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest3.hash = tx.getHash();
        addTransactionRequest3.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest3.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity3 = transactionService.addNewTransaction(addTransactionRequest3);
        Assert.assertTrue(responseEntity3.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity3.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        tx = createTransactionsDataFromJSON(4);
        AddTransactionRequest addTransactionRequest4 = new AddTransactionRequest();
        addTransactionRequest4.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest4.hash = tx.getHash();
        addTransactionRequest4.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest4.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity4 = transactionService.addNewTransaction(addTransactionRequest4);
        Assert.assertTrue(responseEntity4.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity4.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        tx = createTransactionsDataFromJSON(5);
        AddTransactionRequest addTransactionRequest5 = new AddTransactionRequest();
        addTransactionRequest5.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest5.hash = tx.getHash();
        addTransactionRequest5.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest5.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity5 = transactionService.addNewTransaction(addTransactionRequest5);
        Assert.assertTrue(responseEntity5.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity5.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        tx = createTransactionsDataFromJSON(6);
        AddTransactionRequest addTransactionRequest6 = new AddTransactionRequest();
        addTransactionRequest6.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest6.hash = tx.getHash();
        addTransactionRequest6.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest6.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity6 = transactionService.addNewTransaction(addTransactionRequest6);
        Assert.assertTrue(responseEntity6.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity6.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));


        tx = createTransactionsDataFromJSON(7);
        AddTransactionRequest addTransactionRequest7 = new AddTransactionRequest();
        addTransactionRequest7.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest7.hash = tx.getHash();
        addTransactionRequest7.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest7.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity7 = transactionService.addNewTransaction(addTransactionRequest7);
        Assert.assertTrue(responseEntity7.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity7.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        tx = createTransactionsDataFromJSON(8);
        AddTransactionRequest addTransactionRequest8 = new AddTransactionRequest();
        addTransactionRequest8.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest8.hash = tx.getHash();
        addTransactionRequest8.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest8.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity8 = transactionService.addNewTransaction(addTransactionRequest8);
        Assert.assertTrue(responseEntity8.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity8.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        tx = createTransactionsDataFromJSON(9);
        AddTransactionRequest addTransactionRequest9 = new AddTransactionRequest();
        addTransactionRequest9.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest9.hash = tx.getHash();
        addTransactionRequest9.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest9.senderTrustScore = tx.getRoundedSenderTrustScore();
        ResponseEntity<Response> responseEntity9 = transactionService.addNewTransaction(addTransactionRequest9);
        Assert.assertTrue(responseEntity9.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity9.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));



        Executors.newSingleThreadScheduledExecutor().execute(() -> { });
        try {
            log.info("CotiNodeTest is going to sleep for 20 sec");
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            log.error("error ", e);
        }

        ConfirmationData confirmedData = confirmedTransactions.getByHash(addTransactionRequest0.hash);

        ConfirmationData unconfirmedData = unconfirmedTransactions.getByHash(addTransactionRequest0.hash);

        Assert.assertNotNull(confirmedData);
        Assert.assertNull(unconfirmedData);

    }

    @Test
    public void bTestBadScenarioNewTransactionNegativeAmount(){
        String baseTransactionHexaAddress = TestUtils.getRandomHexa();
        Hash fromAddress = new Hash(TestUtils.getRandomHexa());

        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress, 3);
        replaceBalancesWithAmount(fromAddress, new BigDecimal(2));
        ResponseEntity<Response> badResponseEntity = transactionService.
                addNewTransaction(createRequestWithOneBaseTransaction(new Hash("AB"), fromAddress,
                        new Hash(baseTransactionHexaAddress), new BigDecimal(3)));
        Assert.assertTrue(badResponseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        Assert.assertTrue(badResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_ERROR));

    }


    @Test
    public void cTestBadScenarioNotEnoughSourcesForTcc(){
        Hash fromAddress = new Hash(TestUtils.getRandomHexa());
        updateBalancesWithAddressAndAmount(fromAddress, new BigDecimal(100));


        String baseTransactionHexaAddress = TestUtils.getRandomHexa();
        BigDecimal plusAmount = new BigDecimal(50);
        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress, plusAmount);
        ResponseEntity<Response> goodResponseEntity = transactionService.
                addNewTransaction(createRequestWithOneBaseTransaction(new Hash("AC"), fromAddress
                        , new Hash(baseTransactionHexaAddress), plusAmount));
        Assert.assertTrue(goodResponseEntity.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(goodResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        String baseTransactionHexaAddress2 = TestUtils.getRandomHexa();
        replaceBalancesWithAmount(fromAddress, new BigDecimal(50));
        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress2, 60);
        ResponseEntity<Response> badResponseEntity = transactionService.
                addNewTransaction(createRequestWithOneBaseTransaction(new Hash("AD"),
                        fromAddress, new Hash(baseTransactionHexaAddress), new BigDecimal(60)));
        Assert.assertTrue(badResponseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
        Assert.assertTrue(badResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_ERROR));

        ConfirmationData confirmationData = confirmedTransactions.getByHash(new Hash("AD"));
        Assert.assertNull(confirmationData);

        try {
            log.info("CotiNodeTest is going to sleep for 20 sec");
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        confirmationData = confirmedTransactions.getByHash(new Hash("AC"));
        Assert.assertNull(confirmationData); // The transaction doesn't have enough sources before it

    }

    @Test
    public void dTestSimpleRollBack(){
        Map<Hash, BigDecimal> preBalanceMap = balanceService.getPreBalanceMap();
        Hash address1 = new Hash("ABCD");
        Hash address2 = new Hash("ABCDEF");

        BaseTransactionData btd1 = new BaseTransactionData(address1, new BigDecimal(5.5), address1, new SignatureData("", ""), new Date());
        BaseTransactionData btd2 = new BaseTransactionData(address2, new BigDecimal(6.57), address2, new SignatureData("", ""), new Date());
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.add(btd1);
        baseTransactionDataList.add(btd2);
        // balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);
        updateBalancesWithAddressAndAmount(address1, new BigDecimal(5.5));
        updateBalancesWithAddressAndAmount(address2, new BigDecimal(6.57));

        balanceService.rollbackBaseTransactions(baseTransactionDataList);

        Assert.assertTrue(preBalanceMap.get(address1).compareTo(BigDecimal.ZERO) == 0);

        Assert.assertTrue(preBalanceMap.get(address2).compareTo(BigDecimal.ZERO) == 0);

    }

    private void updateBalancesWithAddressAndAmount(Hash hash, BigDecimal amount){
        Map<Hash, BigDecimal> balanceMap = balanceService.getBalanceMap();
        Map<Hash, BigDecimal> preBalanceMap = balanceService.getPreBalanceMap();
        if (balanceMap.containsKey(hash)) {
            balanceMap.put(hash, amount.add(balanceMap.get(hash)));
        } else {
            balanceMap.put(hash, amount);
        }
        if (preBalanceMap.containsKey(hash)) {
            preBalanceMap.put(hash, amount.add(preBalanceMap.get(hash)));
        } else {
            preBalanceMap.put(hash, amount);
        }
    }

    private void replaceBalancesWithAmount(Hash hash, BigDecimal amount){
        Map<Hash, BigDecimal> balanceMap = balanceService.getBalanceMap();
        Map<Hash, BigDecimal> preBalanceMap = balanceService.getPreBalanceMap();

        balanceMap.put(hash, amount);
        preBalanceMap.put(hash, amount);


    }

    private AddTransactionRequest createRequestWithOneBaseTransaction(Hash transactionHash, Hash fromAddress, Hash baseTransactionAddress, BigDecimal amount){
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();

        BaseTransactionData baseTransactionData =
                new BaseTransactionData(new Hash(CryptoUtils.getPublicKeyFromPrivateKey(BigInteger.valueOf(123)).toByteArray()),
                        amount, baseTransactionAddress,
                        signatureMessage, new Date());


        BaseTransactionData myBaseTransactionData =
                new BaseTransactionData(fromAddress, amount.negate()
                        , new Hash("AB"),
                        signatureMessage, new Date());


        baseTransactionDataList.add(baseTransactionData);
        baseTransactionDataList.add(myBaseTransactionData);


        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        addTransactionRequest.baseTransactions = baseTransactionDataList;
        addTransactionRequest.hash = transactionHash;
        addTransactionRequest.transactionDescription = transactionDescription;
        return addTransactionRequest;
    }


    private List<BaseTransactionData> createBaseTransactionRandomList(int numOfBaseTransactions){
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        Hash myAddress = new Hash(TestUtils.getRandomHexa());
        updateBalancesWithAddressAndAmount(myAddress, new BigDecimal(100 * numOfBaseTransactions));

        for (int i = 0; i < numOfBaseTransactions; i++) {
            privatekeyInt++;
            BigDecimal amount = new BigDecimal(TestUtils.getRandomDouble()).setScale(2, BigDecimal.ROUND_CEILING);
            BigInteger privateKey = BigInteger.valueOf(privatekeyInt);
            BaseTransactionData baseTransactionData =
                    new BaseTransactionData(new Hash(CryptoUtils.getPublicKeyFromPrivateKey(privateKey).toByteArray()), amount
                            , new Hash(TestUtils.getRandomHexa()),
                            signatureMessage, new Date());

            BaseTransactionData myBaseTransactionData =
                    new BaseTransactionData(myAddress, amount.negate()
                            , myAddress,
                            signatureMessage, new Date());


            baseTransactionDataList.add(baseTransactionData);
            baseTransactionDataList.add(myBaseTransactionData);

        }
        return baseTransactionDataList;
    }



    public TransactionData createTransactionsDataFromJSON(int index){
        List<String> jsons = new ArrayList<>();
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-3.175070871091607\",\"hash\":\"682d89632f8130ec5bf6603520ec4eb4bf36588cf863cd8bb7347f50b000c173\",\"createTime\":1531728460670,\"signatureData\":{\"r\":\"e3e60ec2ca5bb641ab16868654bda48f9092f6b181003f044c8c898083a20c4e\",\"s\":\"eef372703e69b591669b00b5946a66d294146f94c83c2a4b70483f9a213279eb\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-2.9379535288633374\",\"hash\":\"0b447fabe3e8bb6e812ca07d369108ea9c346e1dbefea24fc721acb5d0c20d08\",\"createTime\":1531728460671,\"signatureData\":{\"r\":\"ed4b652a869c6d62dce91393c139dbaf6d254dece1a4d8a7a8c448065c5e6293\",\"s\":\"37df924004450c1bc3a09c42075ffdb94f6c09b57486bb70c6a7a4cc5cd71f07\"}},{\"addressHash\":\"45f6edd843fa5a32d87a239d2bdb01facb94d857c6d473fd9e374ad665d036595b660a6bd76759bc6e409785ca54c3bf525e4a2b82e656f6e916d8c1d75a32d36fae2f9c\",\"indexInTransactionsChain\":2,\"amount\":\"6.1130243999549444\",\"hash\":\"15dfaade5a3614d235efa70eeb85cd08e3c5c65021cc6ffa27e144f0e0cff117\",\"createTime\":1531728460672}],\"createTime\":1531728460669,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"0858e048a1640d4cd2caf3751a3229c09a82578df0324f16006f88a65bf600e7\",\"senderTrustScore\":67.87087441231779}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-9.787902229397242\",\"hash\":\"2f9ffa0464a0a4387451a266ff432b6ba1f4d92faccc9dbbbe1e5bba2d58f472\",\"createTime\":1531728460672,\"signatureData\":{\"r\":\"942db1786598b9f0c72e1b1ced050b3baf2bcc31eb768646eb5cd1e27b3a7ecc\",\"s\":\"2abd548f615417914c63a14cc74eafe48dcd476b4ae5e721ab9d5187b6079ccd\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-3.6980214302821635\",\"hash\":\"754a3a315267b2a7782b94f66bdcb68d17f7e836ef2ebe696566c767e8bc9f11\",\"createTime\":1531728460673,\"signatureData\":{\"r\":\"afd5c4656d8cd8c3a8a0b143760d84bf951964f8fe4ef5129a9c238772b5d5bc\",\"s\":\"74a16dc88c8ec5446b58dc0ef0d0dceed2fa6208bd00972b49ce5ed157591106\"}},{\"addressHash\":\"c070533006585dd51459702c7d51370b765ae3f976e6758e499e278c155360c99a0c9559d5d54ccd9ae80fe4d84b3005ad1801942f4c7a56bd815a6863204416d0d68b03\",\"indexInTransactionsChain\":2,\"amount\":\"13.4859236596794055\",\"hash\":\"dc6f4bd1831e3a54a17b1c495e09de4483f953f224f25a93071c8a66b8148726\",\"createTime\":1531728460673}],\"createTime\":1531728460672,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"bfcabcf0fa26a7044fadb4f0694ad08866fd365491c0358bb18f65db2f104b6b\",\"senderTrustScore\":68.27456579583006}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-9.32380028995929\",\"hash\":\"272841a7cda70c5cba2f7eda32447c0295ada06e4d1a59ebc3a8106392d37844\",\"createTime\":1531728460674,\"signatureData\":{\"r\":\"41b20ed5eec1f152c921e72af26274b379293a5c39dd85fd3d1d825aeddb2dfd\",\"s\":\"da2918295bd45966feca7eea2c6fd1f5faeb27afddbd4c521ae6a949b92a0bcd\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-3.9371586796438027\",\"hash\":\"d79618b18681d21747bfaef385b8cededb9a17261502092f7c0f8990b1adadd7\",\"createTime\":1531728460676,\"signatureData\":{\"r\":\"82293bf2e0dd5540cb8319dfde21bf0be73de81f1cc57d48c20529ac66926a82\",\"s\":\"f6228ffc59f8116503625a71a2d715c09eb022e9d1a1d5dfb93975c69ed44284\"}},{\"addressHash\":\"fa8b0a7f24186aed92c012ef0992cc2dd9f821fe1acdc7019c8083713da20a366e5e5980a2ae317775d3fe13ce6ffdc13ce17b3756de9322e2a646d036fff041f1f6305f\",\"indexInTransactionsChain\":2,\"amount\":\"13.2609589696030927\",\"hash\":\"5ad0bf8794a3cbe517e0ad2c34f94a8f200c7439b3ecb97d767911caac0f72af\",\"createTime\":1531728460676}],\"createTime\":1531728460674,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"a75b962c322c15a492ed8d074254641a40a24fabf960bb64108867223e7c6c44\",\"senderTrustScore\":81.27891592942882}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-5.123435679079721\",\"hash\":\"d86a975be8a4f8221f50fb302fdb0e63e275f8ee64a32c8c4df8ffdf3c467650\",\"createTime\":1531728460677,\"signatureData\":{\"r\":\"d20475ea848397b8978a689c763d38af8eddd0313b0960b61323c9b3a7f3f363\",\"s\":\"3abffcf4912c0f49ac5a6bb990a22be9103e6cfef4bf4b351d359c13759679ab\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-3.819871789841535\",\"hash\":\"4f084b78ffc24ff219382a1fce31567954a6d3db64ac2e9884b95d3a1da3fb60\",\"createTime\":1531728460679,\"signatureData\":{\"r\":\"3cfb5fd881a2a839fa6824f068bd002d4340a49befef55dc5085c39392007b68\",\"s\":\"7893a5dca3634465f3eb82b7fb8f760090fb373b432c151835c3f419e537d952\"}},{\"addressHash\":\"23cfa34cc6f9bea3b05cb3f8819d24c15003127f10f3e38c2f5615b8e0ca18ca58cae56ff7c3f39ac18015a0035e365bfe35831367d0fa11fce8ca9d12eb2db555dfd968\",\"indexInTransactionsChain\":2,\"amount\":\"8.943307468921256\",\"hash\":\"dee052fa6c7dff3a5c46da32de61d9472ab112757dbe3b2814f0c9d44c262809\",\"createTime\":1531728460679}],\"createTime\":1531728460676,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"9a198e1786898eff75f4f64cb0480cb18ed14a254fcb18c9e05a2fe12ca0d446\",\"senderTrustScore\":93.597246056774}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-3.5236940770488325\",\"hash\":\"966b63bd6b93d4cd4ebea883f8455bd786ee967dca623931e09d6e29a3105b2c\",\"createTime\":1531728460680,\"signatureData\":{\"r\":\"3150395a501bd4befada5f3d99b872589e551b2ef7641700978b24e0e21cdf79\",\"s\":\"1458007534afa51f587ef742fcff0e6fa44b94dea2c5e299d7cab85fa3c2d6d5\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-6.047094025861061\",\"hash\":\"4ab96cb944a52b0cb04322b86656df00cf5bbe7b7471a0edbdd50dbc70c3fb8f\",\"createTime\":1531728460680,\"signatureData\":{\"r\":\"29302ecc9eb23a5827d3797f99c1dfac9813625416d2af6a25a35b7f81783aea\",\"s\":\"8342ccb9bd450354fd669107ed91a02d2b5098a9de947a3fa9b9c4b35c6d57e6\"}},{\"addressHash\":\"722c82632c880b842433efa262e39630583d8dbc567aa4f83c76001dd879902628e042637cfcf5b021699714dca2181a7b7c98d9e1270d2101c383333416d38629eb9df4\",\"indexInTransactionsChain\":2,\"amount\":\"9.5707881029098935\",\"hash\":\"74026d003f435f03d2c94a79b05063aedff9dfbc100b7c9732f6cd4c14c782b2\",\"createTime\":1531728460680}],\"createTime\":1531728460679,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"7c1378168e2e08508037930e79feab2bcf0b1e118275a484cbcfb334a6665705\",\"senderTrustScore\":71.10533086809903}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-6.870274707433019\",\"hash\":\"a34d4a6acd9f33b110a3fa288b27536986ccf2322f8d65602ae697c5593e9e52\",\"createTime\":1531728460680,\"signatureData\":{\"r\":\"c742fe453574ff08cc2f0bb5efc0c9912f9dfddc0b6f8831fc8cd5f9b88c45f\",\"s\":\"786084e1a2fc8bc6e66e6bea959894885ca0fd9dd3fd346de4551c5a91e431f6\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-10.133515196082909\",\"hash\":\"626bc56a4212a64cd0fcbc117f9f66877c1e23fcae91653a58a65b853390a626\",\"createTime\":1531728460681,\"signatureData\":{\"r\":\"4acd06452731223fbce3640224c357906c4dc38c498433eb0b337621f83c0658\",\"s\":\"f1243ebfaeb7c760e705ce6fb4ef32f814651dc0aef3ec043b2ac5c387998f9b\"}},{\"addressHash\":\"ae70bbc5c935982db8a57222ae1e0d3354fdcbf6ea02c39e77f396fa07a3e8edeef9679e5362cca291f9bbb1924be27511780de865d4a59b0ad483cf6b97e2fb6fdb55ce\",\"indexInTransactionsChain\":2,\"amount\":\"17.003789903515928\",\"hash\":\"1eb0c25801743fd43ac47322d29a94a647fd3cffa4aa4b34a975f6dc6b1b5f08\",\"createTime\":1531728460681}],\"createTime\":1531728460680,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"56755bebf809ab1e5c3187b09b902f0a7ce769f4a253caaddc0108161a800c52\",\"senderTrustScore\":45.64325129687534}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"138ae277d61cf5dd2d52dc9f71b0f71e66ff80859430c3cb353652b0c264b2e2750257cb713f7eacb1209ca15a0cd40f688aa5da8526ecb4ea688ea9753da501ef18975c\",\"indexInTransactionsChain\":0,\"amount\":\"-2.739688186900951\",\"hash\":\"37c21a025368c9e8a57e3969a649022ba87b49e689db5506e36be4c0463a3e0c\",\"createTime\":1531728460706,\"signatureData\":{\"r\":\"cfad608dd132143b01f434c0b329f6179355c0c8b2832e05feb9136422bc703b\",\"s\":\"e97581aefc3e5253ba6b3164951a6ea1a8458b4eb7a6cba29a37a049534211c4\"}},{\"addressHash\":\"b71e445dae57178ab3a76d888650ca9e7b78fb420d1f4f8bc70c7c7aed9af0d11c17f3b68d72d619c4cda7c864899944192c942d63fec2e8251eeadd3f42d5c715e6f0cc\",\"indexInTransactionsChain\":1,\"amount\":\"-7.574356237514387\",\"hash\":\"8b841502603573cda3de4024d43906e057a67a8c1bdcf087ffa3be95ea768c2b\",\"createTime\":1531728460707,\"signatureData\":{\"r\":\"430d1ffe67aff29c887ca8711365be71356d0f7d1cef1795a86dc44e652352c7\",\"s\":\"b49912b17fd69b3809e312608c40653ae0c46ce720f94f49dcf7682d3d3ecb1a\"}},{\"addressHash\":\"e3a0999d7137ff6c33d12674eafe6775f32c5056d5d96736d4af35a65be8038f7f3ec76e61491ec5a38f115e02773be6b4c0857b072f8b577ecdcb92f50dadfc41cedfef\",\"indexInTransactionsChain\":2,\"amount\":\"10.314044424415338\",\"hash\":\"2203b9aae2bb591221b09d72e730e2369209aef85db4426bdf6f56385e0ce42f\",\"createTime\":1531728460707}],\"createTime\":1531728460705,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"b65bb00519352fa1ce984ae0d7af39cd697a010c2bd75fc6d227ca670d83714d\",\"senderTrustScore\":46.81123688113015}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-6.446783851607105\",\"hash\":\"0e9592c7561a37f66c09a30e67691bd477ae8f40634792e32c4bcce3f8713b7c\",\"createTime\":1531728460698,\"signatureData\":{\"r\":\"fd7ce94d837a1f77a3da5e59f861375cbf1c9453c1bdf7950de75fab9496f57f\",\"s\":\"f39c5eb397b583ef28d5b52dd0d14cad099b8e9d059e1cdb130b02a8b78ab955\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-4.717246742826679\",\"hash\":\"5612dd724e68a9743853e777fec30d35006e9dbe174c2b45464ba10eca26bdf4\",\"createTime\":1531728460701,\"signatureData\":{\"r\":\"fb453422decae39f0c84be99c94d53cb44eaecf4bd99937c9873182236ebf22f\",\"s\":\"b0a6e77ba2c564d1a432e6c2617123d01aefb1bd4b0f759a3a7830eca3f9ad2c\"}},{\"addressHash\":\"ee032a969d933305722b10af82cb6ace972861eda074fc48665b0f6642ab11388921e169cb5ef4791265f536bfe37c7f66e2afed306d005f5b7bd952d094e97cdaf764f4\",\"indexInTransactionsChain\":2,\"amount\":\"11.164030594433784\",\"hash\":\"405e74bb443ca589af835d0ab39cbdc4be6e133741b7405ead6609e91a8c1f48\",\"createTime\":1531728460701}],\"createTime\":1531728460697,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"c1f1c3e2d95c373ce59363864f326ab5eb42bc1bb155ea050e50cb3673cd63c3\",\"senderTrustScore\":77.61830475379378}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-9.262109318119826\",\"hash\":\"dee2fdb8b8d7e653397b2d8a0ee49e45e3ee066b851c03d1a36931696224c57e\",\"createTime\":1531728460692,\"signatureData\":{\"r\":\"20128164cff4c70f29f81bff36b6cf844e010547015f2ad5e1114e4259e9ed86\",\"s\":\"7e7b40ff6ebe6980ec5e859f93ec1c0644ccc0e596ba8044f78b076304dd31ca\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-2.132572274764506\",\"hash\":\"889f86abb16b64b1bc895e583039a153613363c57fc0de4965f4cd7f0d74eb33\",\"createTime\":1531728460694,\"signatureData\":{\"r\":\"af706f67103592bd98a4ed41eb1efbf6147160ad6295590ac16e99df4c228a08\",\"s\":\"a89b4223c6b1f6c5d40d101adf4d2e9d2de97c61a0748f51713dc648f2fd8847\"}},{\"addressHash\":\"fe055e263137b6187e1e172a4c9431e55392fb843bae8820e3a472bad4177190c83fe5a1190a3dd6036b588e41330a06a60bfb695f0355056a13a72e57743a945fc4cda3\",\"indexInTransactionsChain\":2,\"amount\":\"11.394681592884332\",\"hash\":\"8e9c91de8136ed3d7570e98b7ae7d537613e1f5a8ddfe596f730be7771ca10e9\",\"createTime\":1531728460694}],\"createTime\":1531728460691,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"496091134849bf7f0328d12d39a89d15292e97a968301025c3574e670dfa47e0\",\"senderTrustScore\":66.35164499307749}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-2.446674094563247\",\"hash\":\"6c3ab8d7a6e382b323cf96491e08ab4be905e776fc8bde5a5ab4c7505cc6657e\",\"createTime\":1531728460703,\"signatureData\":{\"r\":\"f61b34dcdc278b0b34ae4e6c18e974534ca05a1f8d03303df01f2df405e3df10\",\"s\":\"1725deae8c4362e2ff2bf584d0f5faaaf3ba739ee291ca273a4c44308e59b1ed\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-9.712031902589164\",\"hash\":\"7b9bc7f0c1572079c1cdd32c6fdc7f6372cab596125343c5844dc33f4ab7f606\",\"createTime\":1531728460704,\"signatureData\":{\"r\":\"5dfd2c769d80b5d566c8c4d42280617b521c00b41cc71bac73ff28bdfeb13276\",\"s\":\"69345ffff37de313be55de9a899b21a914017d2c81d5d79f4345556dd0d90c56\"}},{\"addressHash\":\"b34ab05ff47de17ba79165c038bf1b75c2dfe7aac4802bc3bb2353e04a499f67e9e154b79ddb30eb307981436ba47d70cc5320a91894e579107a6865ee784d79bd7b74fd\",\"indexInTransactionsChain\":2,\"amount\":\"12.158705997152411\",\"hash\":\"2289e8db23978e9f7887605ad21898c9072bf50d3c7a3d403cb3da714c85f841\",\"createTime\":1531728460705}],\"createTime\":1531728460702,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"da8c06d18b23a4a34bf67fa132675973d1f20655f358bd1f74e6009bccb9db1b\",\"senderTrustScore\":93.2946202849655}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-5.2652328929526435\",\"hash\":\"6a25bf7913fb6dde0d8b1ef70b6a23c2b120fad185ba432c4365b4d6b0beec24\",\"createTime\":1531728460682,\"signatureData\":{\"r\":\"a5d36056e5bbeca314b8db4974c49afb9121f1980f75263a8463437b7ab48959\",\"s\":\"1e7d0da7c51cdf06bcb5cb5519c3592114383d8ea789594e07f434494fc1dfa8\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-4.342582609852785\",\"hash\":\"b0630c62b0f4240cfcbff0637b24fab3933522f825cd537b690ad48361683658\",\"createTime\":1531728460684,\"signatureData\":{\"r\":\"169ce84d8e7c1f2ccd99214645c30d536b3ec2fac3d52854644090f36d321fa\",\"s\":\"688c02f958ad5544610417279d605e1100f21ab2fd7fb3fc2d9ccaf763bd7c6f\"}},{\"addressHash\":\"e573a50d292f9e9dd4c2e23e19f273845cd155cc86ac6d57e75f84fbfa565ab7f595c4d74d165ac7d01ebd122a824ba457ada47bd2c6f50e0208bd4f1e3b07a4b7a2149e\",\"indexInTransactionsChain\":2,\"amount\":\"9.6078155028054285\",\"hash\":\"f8e28197bae8074c34d6f9d31462236330c01f9117049293bab943500f937e55\",\"createTime\":1531728460685}],\"createTime\":1531728460681,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"9a8d1b70d1a09c915ea64707f1274f0636e60fa6a6e6d1b0e55f190bc2005336\",\"senderTrustScore\":78.45996968152345}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"0d2fc1bffe53a5d0cf433f49563e4d2d8c0b72ae05f38acb0d019b0cd5d7c751d8de6a7362f63885f961a540d0579a255b6ac43113f46f8b1aade75f2b563c4cf09e3b9a\",\"indexInTransactionsChain\":0,\"amount\":\"-4.91040569546905\",\"hash\":\"98f24457c09764a8544ec82fd15bb029196a6618b5e2589db5a622615bda1261\",\"createTime\":1531728460686,\"signatureData\":{\"r\":\"622fecd180a7d9863ab3720d165e7a1bc69430e0d5d4a593bac2e569e07c87f5\",\"s\":\"ad5fb1ba7a5b8528a2a5881004b1ace9e7f906f1e7740ca487428dd75ce18c3f\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"indexInTransactionsChain\":1,\"amount\":\"-7.079696239797578\",\"hash\":\"b54d73dbbaa72c122fa72dedcd1753e4a8b4b5ea276cd1ce27a9d779b9868bb7\",\"createTime\":1531728460689,\"signatureData\":{\"r\":\"2798beb99d595d9827e348718f224558f1f2517bc37a0ce4ab28de68aaad0c84\",\"s\":\"71178cf75f7d35ae77d21d7c61efe6535b49b31a53cf59a1f4e5b9fcd6e8b28\"}},{\"addressHash\":\"73b3823a6d829657dc4438d399d56b206d2b9059f4d3536713739394cd1aa7e0d411b43048aaa2356cb48ce10b5e5901609a0556cd8d1db3237e136d981367e81f3acb1c\",\"indexInTransactionsChain\":2,\"amount\":\"11.990101935266628\",\"hash\":\"94006d1607644429c39ef47d1036b2095c433a6adb68f9b0e83de3433d6f4044\",\"createTime\":1531728460689}],\"createTime\":1531728460685,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"d7c7d82cde3fcba2224c34fc2519e6006798c032f9ed806c853bfa6f83ed7083\",\"senderTrustScore\":90.89623339016117}");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = null;
        try {
            txData = mapper.readValue(jsons.get(index), TransactionData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txData;
    }

    public TransactionData createTransaction3DataFromJSON(){
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '36b0e1b46556cb8e7a6fb1cf297bc799381d40bc83f48986dc9ac0dbdd52ce4ea894c517f635e8a4c0943f9a2c1d46f6ce55b34c277b7cf70e0704a7c6f4d144',\n" +
                "\t\t\t'createTime': 1531325119553,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'e33794aee374bc1a949a2aa365d056ad57efdb193d948bd51ea2a712d06e862b',\n" +
                "\t\t\t\t's': 'f9f3f79c0ed0609dff9894aba3a8d800c445c0e4885cd739cb4b69b6d0ed03f6'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '9eff5b4ed178c83af8c3b57cfbca4293728d52e188a947930437b0a1aa0a8bb396e6782667bfaa488400042c65098f0ac4f01863bbb32cb3e3cd1f92e5bff077',\n" +
                "\t\t\t'createTime': 1531325119557,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6dba0a0e7e620f5fca08ba10e3b30946c31f77af0851a82ec87511c0ef7800c',\n" +
                "\t\t\t\t's': '9edb9bd30fe90916ae10c9461e8626b4e0aba668f6c43bb7ecc752889989c3c1'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'fce287772288c65be41ea1e607b8012b1f46cfd05bacc921a736db58e9a9d28dc215e6f231592688978cdf1a305b79526450d399436c0e927b473b1206878f88',\n" +
                "\t\t\t'createTime': 1531325119558,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'c159fdcb2260c65264aaac9c4ee1274ba8b0173bc039645dd866f698a13183a4',\n" +
                "\t\t\t\t's': '8e974f8141c4e5bc401f51c42881946dca2aed9a9d2efb1acf61afd21150a4c0'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '63a5a3117695db3c66bd7015b82254a3b0a40fae64820f1d2e2e40d5bffe187b2c29b694e97808459d9fd70cc73b9793d09807cf89b6772c6ee0cf2654e12d90',\n" +
                "\t\t\t'createTime': 1531325119559,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '16a90ab949412ce2f4627bab4808c65aa67d07976901cd181f864c114a109c69',\n" +
                "\t\t\t\t's': '1ec87f03a3b4ea5be2199c57ac6858acf698c940e7d0e855e70f0b1a8be2657e'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '16ae3ece9fcac1268f0407fcfb6402e568a917ea26ede90415f986b4337244bbf84fdcb07c2761d6886f4bd1eb424be1860bc9a0b2ace87adb908ef7864d1b23',\n" +
                "\t\t\t'createTime': 1531325119560\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t'createTime': 1531325119544,\n" +
                "\t'baseTransactionsCount': 5,\n" +
                "\t'transactionDescription': 'test',\n" +
                "\t'hash': '371d4730c61f94c2204c1cd29015f26ff588f49d5263aa84f408bf29a0188b4f19f720e3be2533c777a7ac8de5cf5787c6ec40d8761c23fc95b67d52f8e61cc3',\n" +
                "\t'senderTrustScore': 64.71902931299373\n" +
                "}";


        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        TransactionData txData = null;
        try {
            txData = mapper.readValue(jsonOfTransaction, TransactionData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return txData;
    }

}


