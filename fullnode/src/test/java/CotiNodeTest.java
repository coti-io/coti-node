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

        TransactionData tx = createTransaction1DataFromJSON();
        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        addTransactionRequest.baseTransactions = tx.getBaseTransactions();
        addTransactionRequest.hash = tx.getHash();
        addTransactionRequest.transactionDescription = tx.getTransactionDescription();
        addTransactionRequest.senderTrustScore = tx.getRoundedSenderTrustScore();
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            propagationService = null;
        });
        ResponseEntity<Response> responseEntity = transactionController.addTransaction(addTransactionRequest);
        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        tx = createTransactionsDataFromJSON(0);
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

        ConfirmationData confirmedData = confirmedTransactions.getByHash(addTransactionRequest.hash);

        ConfirmationData unconfirmedData = unconfirmedTransactions.getByHash(addTransactionRequest.hash);

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

    public TransactionData createTransaction1DataFromJSON(){
        String jsonOfTransaction = "{\n" +
                "\t'baseTransactions': [{\n" +
                "\t\t\t'addressHash': '54e43a7b3485e1dca039888b4cf1951caa737c01219f71db01fa31c73afa745645137e4473db82e1986b7c590d093eb32d613924f5a5187e5f2ff4395bbe5eb55203282c',\n" +
                "\t\t\t'indexInTransactionsChain': 0,\n" +
                "\t\t\t'amount': '-30.000000000000000000',\n" +
                "\t\t\t'hash': '36b0e1b46556cb8e7a6fb1cf297bc799381d40bc83f48986dc9ac0dbdd52ce4ea894c517f635e8a4c0943f9a2c1d46f6ce55b34c277b7cf70e0704a7c6f4d113',\n" +
                "\t\t\t'createTime': 1531325119553,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'e33794aee374bc1a949a2aa365d056ad57efdb193d948bd51ea2a712d06e862b',\n" +
                "\t\t\t\t's': 'f9f3f79c0ed0609dff9894aba3a8d800c445c0e4885cd739cb4b69b6d0ed03f6'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '988149bfdcaf6ebc115d2370c159988223d889e0142fecf6dccc10a61763e1b7cc5caa07a32ec65f2105dedc878f1214a5e02db7bfd5c30591405dbd7ac720a78ceb7e7b',\n" +
                "\t\t\t'indexInTransactionsChain': 1,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '9eff5b4ed178c83af8c3b57cfbca4293728d52e188a947930437b0a1aa0a8bb396e6782667bfaa488400042c65098f0ac4f01863bbb32cb3e3cd1f92e5bff095',\n" +
                "\t\t\t'createTime': 1531325119557,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '6dba0a0e7e620f5fca08ba10e3b30946c31f77af0851a82ec87511c0ef7800c',\n" +
                "\t\t\t\t's': '9edb9bd30fe90916ae10c9461e8626b4e0aba668f6c43bb7ecc752889989c3c1'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': 'fba6bdf0937c21ba35817bab7781e8f174964768706e63b87e63187c45b0e84f5837f092b2197d42d84d3608fe59aaeeda64aceb45938b16e6a3b6b5ef28dbf20f8a9f74',\n" +
                "\t\t\t'indexInTransactionsChain': 2,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': 'fce287772288c65be41ea1e607b8012b1f46cfd05bacc921a736db58e9a9d28dc215e6f231592688978cdf1a305b79526450d399436c0e927b473b1206878f2b',\n" +
                "\t\t\t'createTime': 1531325119558,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': 'c159fdcb2260c65264aaac9c4ee1274ba8b0173bc039645dd866f698a13183a4',\n" +
                "\t\t\t\t's': '8e974f8141c4e5bc401f51c42881946dca2aed9a9d2efb1acf61afd21150a4c0'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '4f4c693378d2a5ad9c633fb92e36bf003df06ab743c75a80624e0e912b7b5fa58f69d0bd655157b5e216e72a4d4f14e29bb7c0a996d14fb34f76adefbae4ea1e29dd9316',\n" +
                "\t\t\t'indexInTransactionsChain': 3,\n" +
                "\t\t\t'amount': '-10.000000000000000000',\n" +
                "\t\t\t'hash': '63a5a3117695db3c66bd7015b82254a3b0a40fae64820f1d2e2e40d5bffe187b2c29b694e97808459d9fd70cc73b9793d09807cf89b6772c6ee0cf2654e12ddc',\n" +
                "\t\t\t'createTime': 1531325119559,\n" +
                "\t\t\t'signatureData': {\n" +
                "\t\t\t\t'r': '16a90ab949412ce2f4627bab4808c65aa67d07976901cd181f864c114a109c69',\n" +
                "\t\t\t\t's': '1ec87f03a3b4ea5be2199c57ac6858acf698c940e7d0e855e70f0b1a8be2657e'\n" +
                "\t\t\t}\n" +
                "\t\t}, {\n" +
                "\t\t\t'addressHash': '016d30272b03183635d14f9c45754c1bdb32b7b78d4c60031570f9b367a1e057d1d7f88736a745f41170e5edc1689e6f0f64ed43ea3b6dc19d17e4d7e2e83b32fcb79dc0',\n" +
                "\t\t\t'indexInTransactionsChain': 4,\n" +
                "\t\t\t'amount': '60.000000000000000000',\n" +
                "\t\t\t'hash': '16ae3ece9fcac1268f0407fcfb6402e568a917ea26ede90415f986b4337244bbf84fdcb07c2761d6886f4bd1eb424be1860bc9a0b2ace87adb908ef7864d1b01',\n" +
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

    public TransactionData createTransactionsDataFromJSON(int index){
        List<String> jsons = new ArrayList<>();
        jsons.add("{'baseTransactions':[{'addressHash':'8ad96fcfffceaab17535beca4f33d44a6689ce90e15ec2be27e82ad9da7d59b520dfb7e35350cb1a0685cf2bc0573b9839b102746d134bcf36c779a41b9ea76a3aef6a29','indexInTransactionsChain':0,'amount':'-10.2055114792445','hash':'732a666191c8a28e96d5901d38170e43f3a98e00e135ab109d92003b510a05fc34a6599f528a7041abeb44d5ea3c5bc2c74f1162344aa3cd7e430199a9a7ce09','createTime':1531408484073,'signatureData':{'r':'8f4b42b9c784a7ee0afefd265b82a73f1877327a56af0b3fb070c3a4055d8dc9','s':'a12d0a67b651f2568471e887887a5dc0f77600c2d11ef6dd9d77c2bdaf135ee3'}},{'addressHash':'5b5aa17e12a72111fce404e3ca2df38e81a419b43323b449a41cb7b74151d1e0732f7c9d912658500d8abad2e9352c79a5bc7eeb4e26c0cc9474e11e3c87a2b8862d8436','indexInTransactionsChain':1,'amount':'-7.730313922086292','hash':'4d43f016f7050d9fc3a71a3bba533aae008a66fc3c46fda010567c7ae6f90a6b3c87708f5a67d49f84ad7310862c3eefa58dd10b9768a1efc85bbf750299d436','createTime':1531408484075,'signatureData':{'r':'bd7a4179767250f4895a235c2b0aeac42fa568c46b5bed515cf2feea5486626','s':'7581f08bb6b54f0d302573076fbb7c575753afebbda8ee40bedd11c9d78cf8d1'}},{'addressHash':'cca08b7fec9927091b81f9518fee69b855ade21d816bea391bd9d15ce801f4118ae185347640e8236f243c889cea2c3494ca7cd4728a444b6dbe1e5ecbb2447eb1578905','indexInTransactionsChain':2,'amount':'17.935825401330792','hash':'4949db8a855a5b5a097601bde3b2bc1f01ec470b9e1de8eae7e2d64d85a5cc8122c42a4ff9abf5b9e87b3726b95504ff7f8fb70503b5858cb93452c29e37f225','createTime':1531408484075}],'createTime':1531408484069,'baseTransactionsCount':3,'transactionDescription':'test','hash':'90baa7a6af8021f6e2b0cc1cafaf20b41813101e98d7779c1e9f48add523d0e92f6cef7472de753a4c7b26a4b32c8c4afdd239dea8433e30a82bace0921f2745','senderTrustScore':70.41878011603819}");
        jsons.add("{'baseTransactions':[{'addressHash':'d2aaf904ad17084010f504f274057a6b2e9a2d2eb2eec4335c88b25933875ad8cde3d12bcf84502761d50a62357620870dfe6ef80fa488c0c6fb1c9954e27485c659a548','indexInTransactionsChain':0,'amount':'-3.9035437155330355','hash':'176269c2776577d95696f592bd69e463dc41cfa347dc3958685d6280a09b11d0efe9f3a7e6c41623fa9b17796880ed6577bde70045c9ed69ad175557f0647866','createTime':1531408486659,'signatureData':{'r':'362adbe6bfc427c68f9c82b30d5dad26612fc2d445653c01b5364c3d2d3aa4b5','s':'df801bb385076af7dee3c8978c23896ff2a9c0800a788e6739822bf758956463'}},{'addressHash':'d5bfebb500cd0c0fd5abbc5abe5039b728ff4cbaed617434b710b213be6f88c0a64662fa4649d2f3ffb58bf73396db3b9937a183253bae8e8fdd7c75cadcca40f79afc94','indexInTransactionsChain':1,'amount':'-4.826726917795362','hash':'d31ff254682a7a109653a5ab6c5baa65f8d5eafd315814334048d2b8a1f8c1cbe3070d9d7deece661fcdae8e9da692b669c2149b46b3dd4af1785bbc3426ed65','createTime':1531408486661,'signatureData':{'r':'add4a90361cdeedfff629d2335a148b09bd7edec71fc080922deeec46595de07','s':'b323bc462e8465180333fac94432e8e860e75d49b6c1993248323b810fbe3452'}},{'addressHash':'86b45bb25978ded28150557f182ff3bcbeeebb2ebf3b1d702c53f3263939c528d8d19d0491713bbdbf70b91baec86ef07ae484a865dbecf6e22e800aad368a4fb79783df','indexInTransactionsChain':2,'amount':'8.7302706333283975','hash':'8df447df36bc920fd880987fa215f2ad7b366d1f201e4b9c68828b8e5e4359cecdb92390d6d6047e12e1c042600f19005f096176e1b0610d07809b5d6f3210da','createTime':1531408486661}],'createTime':1531408486659,'baseTransactionsCount':3,'transactionDescription':'test','hash':'c12c22693c9e088d29dfc7b713a169d5aa7160ce679aef6a24ad7ecf2e139f9e87845881003baf9eeb97b7085f9a441e5f806aeff7d8a4f901ae6b15dee2320c','senderTrustScore':59.7463764761776}");
        jsons.add("{'baseTransactions':[{'addressHash':'8ad96fcfffceaab17535beca4f33d44a6689ce90e15ec2be27e82ad9da7d59b520dfb7e35350cb1a0685cf2bc0573b9839b102746d134bcf36c779a41b9ea76a3aef6a29','indexInTransactionsChain':0,'amount':'-10.523422216813858','hash':'d14e9e51496868fa5a84d6d32d5aa1ccad2d81b38ea035e6adcab071cc4abb456b57806fb3b2eae626b61682e593b680c0ea463c1710eb391f6345b278920a52','createTime':1531408484077,'signatureData':{'r':'47c1f7c04344e8f94ff7ad29d87ef3e96e6fa4e8156b3a6f945b52d8dde70d70','s':'62019e7fa93f891fcb068fae22bb7e0c2439597c5ed3f9a553070fa0476f7b48'}},{'addressHash':'5b5aa17e12a72111fce404e3ca2df38e81a419b43323b449a41cb7b74151d1e0732f7c9d912658500d8abad2e9352c79a5bc7eeb4e26c0cc9474e11e3c87a2b8862d8436','indexInTransactionsChain':1,'amount':'-7.104033445474584','hash':'0f847f849ad2f1d5f012fa5d5ee7545ce49ecb808db37f117851fe233c31600498688a7d35ca0923bb66b95ce43e6ee360e182b8f74566c2cdae9d57e7a4a7c4','createTime':1531408484079,'signatureData':{'r':'7fd0e89175166db421a622363c1c9f51179e6ef0814e7e1822f3a5d1472389bc','s':'74aa3b6383706fcb4418c71dd747767e6edcb2d9921b37dadeb5e41670f0b9d7'}},{'addressHash':'93d62abfb3d210d15382e2f7f4368c8bcf134b2e39269995d0dffae535c59719c81b2ed652607d7f861c9e387463c568636fcacb49e44e1a378d565f356b9574a45a9d64','indexInTransactionsChain':2,'amount':'17.627455662288442','hash':'75fbae9c9035e3203e3c43e30bb7343599e83db65d5bc68d7f8bba5f7498ce7c79b881b2f5a735a7397aaab840334835f790f3a8b91bdfabb34d5885d10479f6','createTime':1531408484079}],'createTime':1531408484075,'baseTransactionsCount':3,'transactionDescription':'test','hash':'25a54487a5e43bc70ff47e4155ad1f063a236d9c3db399ec15275d88af91dc09d1684c3c868ae19b961acd916b692197d5e9ae2b90beeb9bea45fafe208a8cf3','senderTrustScore':94.29801260253069}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"56016a13c66c1a224b7e20d557c8dd3285915ce365a0e91b99b492762f80d936a9c4b370663da1bbe410ecef7d83c1a0ee94c4a1ad61171ba31f4f4e010e470d3994c630\",\"indexInTransactionsChain\":0,\"amount\":\"-10.44041679021183\",\"hash\":\"0147b61fd1bcff8e54791cefd64a136241281d2d9ba85e23034ef96b0df33b2656a28ee68078933e75921286c145977886d6aa97a2b2aee07eb140143fd3a646\",\"createTime\":1531408484112,\"signatureData\":{\"r\":\"d703169e3b28eb081610b02bbf1bd31fbe398a1932ff838631f1dbb471dc368b\",\"s\":\"45615acd2256c6a053309ee194fbe295dd7d7f79cb34857d60e3372ce41851d3\"}},{\"addressHash\":\"e157f39d0aa55ec4ff4fd04fa926e0969dba1f47cc57571f5a99a717adf3f6bb9b81259c4af346bd59bc77035922f77fc6ed266c9b007d7e2eb4e56e5dba03a6eceadacf\",\"indexInTransactionsChain\":1,\"amount\":\"-3.205424680047958\",\"hash\":\"44bbca429df37a51523724d290dc4cdaad502aaf436f4f311f44799b1357c63871ee49348278af1ac31cf21f6276624d9226b9b8509553e46f1e00c10d898201\",\"createTime\":1531408484114,\"signatureData\":{\"r\":\"c5ed133412a351e75753c6fddb430cefa31d14834db61c979677dca576b49ca8\",\"s\":\"8362d9ed660efb8d658d0d72581042eccef7dcaf82c2eb7c36b819215c88a5e7\"}},{\"addressHash\":\"f9da4e3ec754e569730aab537066a455fe7c5d0f5380153a851d40ff6f4015c47c851ed1e0ffc5d9795ff775c4237f1a168a0becef2745b80f751a76950106ab846cf71d\",\"indexInTransactionsChain\":2,\"amount\":\"13.645841470259788\",\"hash\":\"0924e142a6556634ca5714a711fa1782a7150ea56c1e08778aaa8f8e26a652978fbeabad659d7de0b4bd5cb05db07515e01192113ba4f797953754287879ea87\",\"createTime\":1531408484114}],\"createTime\":1531408484111,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"f0ccdb7e33d3385bf5ab5b1083ffe319ce34826abb660e9feffc3b1da0689aff563cc15f3bbd0c944074110792bb154c96a3604dc524d1f006fd1063a064cf79\",\"senderTrustScore\":61.844008302088874}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"56016a13c66c1a224b7e20d557c8dd3285915ce365a0e91b99b492762f80d936a9c4b370663da1bbe410ecef7d83c1a0ee94c4a1ad61171ba31f4f4e010e470d3994c630\",\"indexInTransactionsChain\":0,\"amount\":\"-4.159487629174302\",\"hash\":\"e80e67a5d9f6eeed8f37fdb2ad11e560722cead6f158f731c6663a36717d36ce91d19756aceaee541243e48e61b93656eb8b9074d8eab5e25e046311fc7db4fc\",\"createTime\":1531408484118,\"signatureData\":{\"r\":\"5b2639c56514cde8c00d4d637d958573cc91852c3187d2962ec99cf00adda3a0\",\"s\":\"d8e0ec828b991352df9e45f6cb86759b2b6833603625e9c9e0d3ef44af965f2d\"}},{\"addressHash\":\"e157f39d0aa55ec4ff4fd04fa926e0969dba1f47cc57571f5a99a717adf3f6bb9b81259c4af346bd59bc77035922f77fc6ed266c9b007d7e2eb4e56e5dba03a6eceadacf\",\"indexInTransactionsChain\":1,\"amount\":\"-4.685054472012151\",\"hash\":\"d2cf448887f1149d7f6281ce871f690788b622df8ac496c08c555042c685649559a05ae2cf5f2726d342735d52ffadcf6a06a08feaf4ffa19bc15e3e4ca65511\",\"createTime\":1531408484119,\"signatureData\":{\"r\":\"fa1039a8dd3e2f02b25ae0da3d65065bb3f0b9c99e472a63f63716e524019788\",\"s\":\"3531ef35717ee7045d95a61ff160c52667d7b6ea7d8dd0f1d5c90b88916798c9\"}},{\"addressHash\":\"876b6b5a69250ce464f032e86c0ee5b143f77c9ccf6a8c8ea36a42e57ef182ee21b2155feb7d463f47c7d823e06ea99e2bbcef3679ed25e92a57823555a36cac6396de33\",\"indexInTransactionsChain\":2,\"amount\":\"8.844542101186453\",\"hash\":\"3292fd7e423dd04a7010202487357de5bd414332e5f8efee82aa16e0dafbe51298074bb904a53510798b431b0e3be0f07b60c75624fb679a4ca7a2dab76dc356\",\"createTime\":1531408484119}],\"createTime\":1531408484117,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"cb171ac4fa4800d7907b7c2442ed69b31fd5cda31585aa3a6054d3df735e23b1983cc5a6ec37503114fc0f36ced0ee79a76c57213069a9344c54f6604729b39d\",\"senderTrustScore\":47.657713583578754}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"56016a13c66c1a224b7e20d557c8dd3285915ce365a0e91b99b492762f80d936a9c4b370663da1bbe410ecef7d83c1a0ee94c4a1ad61171ba31f4f4e010e470d3994c630\",\"indexInTransactionsChain\":0,\"amount\":\"-7.236900636786599\",\"hash\":\"aed484ce7949d422545daf7567dbcb946f3acd530927fb710b0a607e70627d0a29e50084d0ba89d202c13954010f390ee98eda9712d11fa7def93221a9971f67\",\"createTime\":1531408484122,\"signatureData\":{\"r\":\"e8b6bab8efd08333ec6589e5489fa2e5f7979f793f5afaaebea793a96f1e449d\",\"s\":\"717df9a2e8e90226336484a32b687ac0e00a254b715ba9c22da23cd200651394\"}},{\"addressHash\":\"e157f39d0aa55ec4ff4fd04fa926e0969dba1f47cc57571f5a99a717adf3f6bb9b81259c4af346bd59bc77035922f77fc6ed266c9b007d7e2eb4e56e5dba03a6eceadacf\",\"indexInTransactionsChain\":1,\"amount\":\"-9.49591557029823\",\"hash\":\"d85745a5acc56ac219f39df186010f11266a0200f3bf64b182d5217e638b0ba41b8872375f79cdb6d8f416ccf9a1fcfd662ba1560aa0b2d506a8754ab3a945db\",\"createTime\":1531408484124,\"signatureData\":{\"r\":\"1ae1432eb3a3ffa020f3b5b5fd1caac89caafebdf6bb3a250cf4aa21648760\",\"s\":\"f1b45f291c6b9ab4bdd5b13dea4776f9c9ba5231379fa8300458b0a007a83a8\"}},{\"addressHash\":\"ee15ed62466ae32fa1f8beb0fb73734790b76041bd9807d3e05150c5b25add311e257612a2dc15d607edaca36b4e3ef093dfc6e03c8b105aef65f2002a3c114d9e434c0f\",\"indexInTransactionsChain\":2,\"amount\":\"16.732816207084829\",\"hash\":\"8799ad3c47799d8f3fe3c877792a85a6c1151e2938456227127d616a11c3a7bab78567c9e4b239ea72497bbefe0f47064d249ed8a28b0c0d0ed3ee22fc2f4fca\",\"createTime\":1531408484124}],\"createTime\":1531408484119,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"ed138fc1bcd7c2a71b8b412377e28f7852236a1c24285a8370d5ed69fc143b2670b681cc2b40071b66a9c9ffc388a0b4159c4e938e829239e43e65904e96cdb1\",\"senderTrustScore\":47.20370458575304}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"4ea71e48d3fc61049b22e44c4474b8d8d205bf1c04d764e012a654f6017164c473c4da3a85606c0f0abf0249ecdeaadce98271d4a6cd53d1b6548f006d96d8c3cda2d5cb\",\"indexInTransactionsChain\":0,\"amount\":\"-5.200905216994036\",\"hash\":\"83ef90c33e9c3f34106f634854d42d25f4f1aff2001d5260378d24463f00cc17033afc68b1266e80e3a9e492b0dd694d158b6ddc4daf3c456167dc2016cb3afb\",\"createTime\":1531408484179,\"signatureData\":{\"r\":\"d4d41241fa7b44b893220e582ddace728d620065713419713b2f53a9eb97999d\",\"s\":\"930e9e24099e218083a638b56bee50d7d94f79df68d81e27b7a73d59cfc7197f\"}},{\"addressHash\":\"02c2593026c37079c9366fa895ba8cc19e817f4fafdd4bb582e9cd390ec495e9787b163238ba758fab37c55f84c865322201386af03e5dcf1f165924aa0693d4e1602346\",\"indexInTransactionsChain\":1,\"amount\":\"-6.101335826089901\",\"hash\":\"0d75b25fa14e60697f59504d9d16fe0570c8a4fbaa3d237ec37698d6eed997e3cc99baafa0133f0bc0e98e970c6de73e09b4a0d511e25e6668733241619e7ce6\",\"createTime\":1531408484180,\"signatureData\":{\"r\":\"e7f7bb3b76c29d78efb506b7e63990526023b1ab104e1f73ad2578125dd136a3\",\"s\":\"e97dee21ee662c9aef4f955692a7a4abc1293eea18ff7060a4e2a5f8a19c8b5d\"}},{\"addressHash\":\"3199b77ea59a862b0bb11f29810cdce373d260a6bffed16dc7cc115a0f675acb56194e1271b27bd0e8d770bad676948644ddc9fdbc23f68f760874d6986ded2c31dc6903\",\"indexInTransactionsChain\":2,\"amount\":\"11.302241043083937\",\"hash\":\"602e21c67c79cf17678d4bf5c4d435887a723f583db0d766fbd75bd6a240f246a7168e1590a6ce68896bfdaafb573ca1b736215f70d689095164450abffb7126\",\"createTime\":1531408484180}],\"createTime\":1531408484178,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"7e1825b4a880e4b87ed2a8f2755578b6695711168846cb929ded53c9e84cd866e15f7fad3a0de95377b80b96bf7a8debe0939c34f03d73358005c1d09a2e026d\",\"senderTrustScore\":52.438856936932304}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"4ea71e48d3fc61049b22e44c4474b8d8d205bf1c04d764e012a654f6017164c473c4da3a85606c0f0abf0249ecdeaadce98271d4a6cd53d1b6548f006d96d8c3cda2d5cb\",\"indexInTransactionsChain\":0,\"amount\":\"-4.111072576617641\",\"hash\":\"8a4533bca7ea03a7db0cfe4d003b6d3ae825a75fdc3a838a516802a90a71be9c3bafba1643532c9f8405c4b7e49de05102f475a6d0a312e1d24b5d4e4f15072b\",\"createTime\":1531408484183,\"signatureData\":{\"r\":\"27201948188f1c547459077a2ddfae946d4df76dbe7de12b502895008b136864\",\"s\":\"d79e623fca7a3e67fb6852495599f6fbb78757a3623d6a4b9a601a853be4fae7\"}},{\"addressHash\":\"02c2593026c37079c9366fa895ba8cc19e817f4fafdd4bb582e9cd390ec495e9787b163238ba758fab37c55f84c865322201386af03e5dcf1f165924aa0693d4e1602346\",\"indexInTransactionsChain\":1,\"amount\":\"-7.965316956880333\",\"hash\":\"f510ebb26b48d56a255b5676991e05bcfc50e39b868c526bfbb555db9a40452903542306f2aa9af713ec0e5dd3c22433e391d3e8d765f401e4c4fb5ba334f54f\",\"createTime\":1531408484184,\"signatureData\":{\"r\":\"81e2543f1d863bcfa50bd40d297d2ec2841f6e68e7d0fa19163123391919fde1\",\"s\":\"e64321dd6507d7ebbddfce3c4c711dce1f6f9c982a2875fde259e570e1f7969b\"}},{\"addressHash\":\"1646bc4c8aba60fa6bc49161a7df85b1b3326b3ba79ab4f9b34a47fc9d5ae03bb4d3bcb6552d15e25f38d498ba0b75f642d5784f507731180dacf502183144bbdebbbbb2\",\"indexInTransactionsChain\":2,\"amount\":\"12.076389533497974\",\"hash\":\"d2033eff5e5b8328877ae2c7fe748010f72a25113d5fd5dcc7587ee20eb4c7f54c7ba55425d7b031979de797839ea828ea07427ddf0a614ba0d5cebbfd3a2e21\",\"createTime\":1531408484184}],\"createTime\":1531408484182,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"d1c2c8fa8d797852dff1d7779f9974bada0581865b545c64ab3ed94d0f99a615beca9a70c844c28c2c82bfd7d7855488d55b1475995f8ed7350dc1dbae08cb5d\",\"senderTrustScore\":60.35345525551317}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"4ea71e48d3fc61049b22e44c4474b8d8d205bf1c04d764e012a654f6017164c473c4da3a85606c0f0abf0249ecdeaadce98271d4a6cd53d1b6548f006d96d8c3cda2d5cb\",\"indexInTransactionsChain\":0,\"amount\":\"-6.563352155521251\",\"hash\":\"7efba6a0a579528cfb6cfd7352082f536cfd14d2b9386baa65d2d9d799fd61414b61963ae5f79c9ee7fb8b18c2a3a22f0e90ed46d1a965472651e13195f96c66\",\"createTime\":1531408484185,\"signatureData\":{\"r\":\"be9bad8640675c0d000db6e28f96ac1f4d1b79deb9cd4c57465880dda9125168\",\"s\":\"3dd0458e25e30e7f4526235dd36d055c4bb966511d49633cad0ef2e928d9777a\"}},{\"addressHash\":\"02c2593026c37079c9366fa895ba8cc19e817f4fafdd4bb582e9cd390ec495e9787b163238ba758fab37c55f84c865322201386af03e5dcf1f165924aa0693d4e1602346\",\"indexInTransactionsChain\":1,\"amount\":\"-7.756990624247061\",\"hash\":\"82e28b3597f2cb4b7049cf8614253765571856863e81a37d7a411b13360da13074666dd42b3f8f35bd18887adbe347ea86b6b9455746934b8f34f2e346651107\",\"createTime\":1531408484186,\"signatureData\":{\"r\":\"9487d4f2dfa834e273296c82799a0133085a90a7e025ac44abb7a9b51e8b7a9c\",\"s\":\"210d9872e66d97a77c605464c7d861562a78b45efb6072a9043c3ca986e25fe0\"}},{\"addressHash\":\"c82ac2e1ddc7896e4eec283fdf25cf2e633a39db53b6310e2f41d15ef2f5e39638fdeecb6c33d6da5fb85b57367fa9119d09411c2f5e9037f6f2ca3544546899b88ddbff\",\"indexInTransactionsChain\":2,\"amount\":\"14.320342779768312\",\"hash\":\"f389eea6c9a1864895f9be8c1cac4bf16088e1d013c13a0557d2dbb32e87e1616f447516d680a5760b94eb4018413ea67846ac0421ec5f1ed635c176e984ba36\",\"createTime\":1531408484186}],\"createTime\":1531408484184,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"64192033b8ac74888ffccaff922c11f7be530218b61be5b31f4f13591fddae57f2f224f30e204ff66472a38340dbb430ca8344a9f23c1488f08c3f03ceefa666\",\"senderTrustScore\":94.58527582764788}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"4ea71e48d3fc61049b22e44c4474b8d8d205bf1c04d764e012a654f6017164c473c4da3a85606c0f0abf0249ecdeaadce98271d4a6cd53d1b6548f006d96d8c3cda2d5cb\",\"indexInTransactionsChain\":0,\"amount\":\"-3.22606053179326\",\"hash\":\"ac44a9adee57adedb12d9798dd56d5cd2278d86fdeed8449348bf287954b8dcfc8272c408748a93dc45cdcd72cde172c6c7832a03f2e4c79507724871899f486\",\"createTime\":1531408484187,\"signatureData\":{\"r\":\"f122d9db19e493b6576012795b5262fdb5d5419880fa6348cc376c819d46f3f5\",\"s\":\"31944ca2fbabcb9edb61fd3fbab447f5ce7d7926cd301a9b4309aab05f75afdc\"}},{\"addressHash\":\"02c2593026c37079c9366fa895ba8cc19e817f4fafdd4bb582e9cd390ec495e9787b163238ba758fab37c55f84c865322201386af03e5dcf1f165924aa0693d4e1602346\",\"indexInTransactionsChain\":1,\"amount\":\"-9.323091573558832\",\"hash\":\"69698439efbd5e3908557e495510275064077235f3e22f2cad742ecd4890549a87f1e90044ba238f5678538e6efa7a0bb097f3376716566b58e116495f201362\",\"createTime\":1531408484188,\"signatureData\":{\"r\":\"503dcf6f3e1b648fa079db9d4bffccf62e2b3a813de6113b09d0c01e7e350ca9\",\"s\":\"f00afb5a0ed1efa99cbb64e6949920076fc13c3713e63bb911c590e00e97bea1\"}},{\"addressHash\":\"bcb85981a6f8bb884def5ced82b51f683320c671188c237f220779ade467d70e6d3dc25841c4fd0d3162f8488f6206ca134876780280b1ac103c0d04a3cb5c5cf03ec4ae\",\"indexInTransactionsChain\":2,\"amount\":\"12.549152105352092\",\"hash\":\"1c0bc63d6cefe939f52ded9084082a6f0dd9e4c1a5d1fa3f20815a0b101c44544fdce63e175c4059af83444fbd35ae708f7186ae9a52255caf5fce08f4449903\",\"createTime\":1531408484188}],\"createTime\":1531408484186,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"0e6e5b4ecd72b0ab736f252e23af88742d9b7ef560840232695e4381d026e1f1bfcfea021e12eeb4fa457c52bfa7ec4434ae6c668a206f1ab24fbb71e441031a\",\"senderTrustScore\":61.547325677172196}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"4ea71e48d3fc61049b22e44c4474b8d8d205bf1c04d764e012a654f6017164c473c4da3a85606c0f0abf0249ecdeaadce98271d4a6cd53d1b6548f006d96d8c3cda2d5cb\",\"indexInTransactionsChain\":0,\"amount\":\"-3.1392569342514505\",\"hash\":\"418700eea7972fc1e247eb58b6031b03824cc3bcc5aef1cea375eddca8d3ffeaef5f76a03493452025436bfe7c8b04de196557c3bc29c60880143bf8d85e16bb\",\"createTime\":1531408484189,\"signatureData\":{\"r\":\"455c78f21417958e19b3d3237a42ccd93a8e3dda4629aa113e5269d98338fe71\",\"s\":\"ad5c4f8aa99f978ef1d040e26df9872c7bcd4d76a7351c537accf5f32e7b857e\"}},{\"addressHash\":\"02c2593026c37079c9366fa895ba8cc19e817f4fafdd4bb582e9cd390ec495e9787b163238ba758fab37c55f84c865322201386af03e5dcf1f165924aa0693d4e1602346\",\"indexInTransactionsChain\":1,\"amount\":\"-7.7149321769273325\",\"hash\":\"575340bf184853d699d6bd15a0e920b2d3822915d9409ddc2814abe519742504cb8b472065ddb9fdc5d519d1aa3a7c960d04b00931841d5e0eb637135cdd1439\",\"createTime\":1531408484190,\"signatureData\":{\"r\":\"5495b45cf6232be9d68bbe938c1a6f3e4cfada9b9be6d893980e8d3caee25115\",\"s\":\"76fedf1873898e525ce7e717f8fd40ed85b00ebcef3450ea02a0b0e0bdd642b2\"}},{\"addressHash\":\"4579181512781bb9f841cb678219b1e5f31b9fcb1715af5d7046b01d3e8db7aee8ee0b1bb57c346750a9ca4058b475a654b04aa2a79a917c576ffa2c617224c6cb58828c\",\"indexInTransactionsChain\":2,\"amount\":\"10.8541891111787830\",\"hash\":\"3d0c9473ae2f1f5ab783d9e6dd005128e6df98f9fe863284e74daba35217bd6656500faf4b7e2b759bfb490b0a5d4186224f6ed9400eec3db2c87f98e0e716fb\",\"createTime\":1531408484191}],\"createTime\":1531408484188,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"34cb36ef1735f2acd3b14c7bb8087fcc2786b024670ff832b2a153d7dd7f360e29954dfd9c0b3a7c2de221ec1bab8e4eaacf2877258d9dddcd6552433686abad\",\"senderTrustScore\":49.6906034465942}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"4ea71e48d3fc61049b22e44c4474b8d8d205bf1c04d764e012a654f6017164c473c4da3a85606c0f0abf0249ecdeaadce98271d4a6cd53d1b6548f006d96d8c3cda2d5cb\",\"indexInTransactionsChain\":0,\"amount\":\"-7.054670785429162\",\"hash\":\"b71dccaf27951bb8cf0830a88ffefe691e72e54418186d1fb701c66f5d838b3c8935bfdbf01e9b447953279885a1f902016f2260a41da852d1508d85a576b3c4\",\"createTime\":1531408484192,\"signatureData\":{\"r\":\"a36d7290a6e8b22bb7ebbfcf02a0d5859966cf8751d87fe2ce48b2ddb97540c7\",\"s\":\"5aab0a99a1b4f98eab6d185c359560d58c36b56f15ac384673a380866ef7f3e3\"}},{\"addressHash\":\"02c2593026c37079c9366fa895ba8cc19e817f4fafdd4bb582e9cd390ec495e9787b163238ba758fab37c55f84c865322201386af03e5dcf1f165924aa0693d4e1602346\",\"indexInTransactionsChain\":1,\"amount\":\"-5.674615012200669\",\"hash\":\"7ef567cdf2bb4e4658a0ad9bc974efbbd4d04707fb98875795d2f5d5a2c8efff6838c86c622819c534f3c017d83f82c9770585e27ba2946953227ee35d15fc6c\",\"createTime\":1531408484195,\"signatureData\":{\"r\":\"a7c3ea59d6bb23be4fc11ca2313ddcf80c8119544d0a41e24ed3fd6f511b2c5\",\"s\":\"1defb4071fce76abc4aeaf0f2a5d0e28f716a5b2a41a2091730708ca21904ab2\"}},{\"addressHash\":\"9dd4364a34214ca44c9f40514515e073800d840e88585c82dab27d4979a37b94e8e089be901cffbaba6b09a6d8b3ff5098e30b8cbc4700441a5d0dd010ab3544e4792235\",\"indexInTransactionsChain\":2,\"amount\":\"12.729285797629831\",\"hash\":\"5b3f8cee655dd4602e91c559eea34d280d36899d468ae0ca08d66b6f2ab8c7d7d575ff2d010d117d5c5cd30a23f93486c7ff7082d49410da9f986063e34c13d0\",\"createTime\":1531408484195}],\"createTime\":1531408484191,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"0d22b9ad3cd57468b8234deba2bb31a6dbf89797e37fb1c5908f852ddca916d5eccd2af8a49b22bc144376a3e07f7eb955a0b37beebb47090020e91b9b7fae72\",\"senderTrustScore\":76.19425387250317}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"8ad96fcfffceaab17535beca4f33d44a6689ce90e15ec2be27e82ad9da7d59b520dfb7e35350cb1a0685cf2bc0573b9839b102746d134bcf36c779a41b9ea76a3aef6a29\",\"indexInTransactionsChain\":0,\"amount\":\"-9.0123749649792\",\"hash\":\"bf5c87671d707b743b2a36e40882b655789073c3920efe179b80c33e448c7a6120ec51edb54f183091843291d8029c62631ed1ff19aaa6a3a950411430c44997\",\"createTime\":1531408484044,\"signatureData\":{\"r\":\"4de6a14922cc2a6dd53c511fb80df18d73e355d9309d415fcea1b2c1a43958d9\",\"s\":\"b26649e9a3fd9ef1d41d43851401e31442f8918885dfa949e35237f52a14efb1\"}},{\"addressHash\":\"5b5aa17e12a72111fce404e3ca2df38e81a419b43323b449a41cb7b74151d1e0732f7c9d912658500d8abad2e9352c79a5bc7eeb4e26c0cc9474e11e3c87a2b8862d8436\",\"indexInTransactionsChain\":1,\"amount\":\"-10.048204317253878\",\"hash\":\"d51e626ff93c916099324e42220e99dd39d9df68380cb25255115e0616d9179f5b74aadf884b19b0374d4391f5b7c03e419c35525227adb6abffd35401e84f9a\",\"createTime\":1531408484048,\"signatureData\":{\"r\":\"7ed80a17153ac482b72349727452388571fb307e3bbcb1e19b4da2355ae5ede2\",\"s\":\"747450295285926c0899c94bde2734541b3984ef4f83be6cd12a84fc79212eb8\"}},{\"addressHash\":\"84228641a4ce5885445e09b3550c4e03789d61a77cb035cbcf76a3b77d1ee344c1d4ebd33e4504b81a06f940be242c4c4a471509583e408f7a50e589502e8746988d1951\",\"indexInTransactionsChain\":2,\"amount\":\"19.060579282233078\",\"hash\":\"ea937eb492255ac1ade7498c093b3189b2cab6f4d7487f0dcd7d7f4d687f799d89f31f360987471233e9102276379dd82e262868b034747fda467b688919583d\",\"createTime\":1531408484049}],\"createTime\":1531408484031,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"a6acdeccd748fbfb079a4972a9b67ad4d55f9e96fad94f2a859c14a4aa0370551649995b782790d4f06011b2c85baae8cabbac7e7ee90a0631f648a60ad6cfe5\",\"senderTrustScore\":59.40192335091079}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"8ad96fcfffceaab17535beca4f33d44a6689ce90e15ec2be27e82ad9da7d59b520dfb7e35350cb1a0685cf2bc0573b9839b102746d134bcf36c779a41b9ea76a3aef6a29\",\"indexInTransactionsChain\":0,\"amount\":\"-5.78155030168028\",\"hash\":\"e141680f55e449fdffa9db88ea351e6c4b566d24d51079ba7314e4c690aa1233065b335394247c52ce2a32cc04f750298b26d6a60015c7e2d23b2c89121809f4\",\"createTime\":1531408484081,\"signatureData\":{\"r\":\"a7b46516178715881a0522c6f4979aac45e00270eb25f68be92ede72bc3813cd\",\"s\":\"bbd7166c8a00f3bc743220dde812f4543296c93eabfee23d22aff93a8d6b077f\"}},{\"addressHash\":\"5b5aa17e12a72111fce404e3ca2df38e81a419b43323b449a41cb7b74151d1e0732f7c9d912658500d8abad2e9352c79a5bc7eeb4e26c0cc9474e11e3c87a2b8862d8436\",\"indexInTransactionsChain\":1,\"amount\":\"-5.939413465639374\",\"hash\":\"7cfd856e741ece8624e0da9dda01080f86cf7e1abaa011943bb5e09491358de6729c0398c9fd18e25597792fdba03f8a5a35d47c824e90d10ad440d3e332451f\",\"createTime\":1531408484082,\"signatureData\":{\"r\":\"531b517a385e31b29e2ff12f302d475fbab2c4dc1991fe4017c21670e861909b\",\"s\":\"84bd3b4b5de42da44233ff32cd9bf3beb975bd517f9fa624af4c5962d6406ef2\"}},{\"addressHash\":\"32c34dcea8bc8557865518d0afeef4c6b5a509b5d7b65a582d513ff8908caaf0fa7174ba63c55b82ea20b451a31dec7b47de26a2014ccef775fefec92707d42f697d7194\",\"indexInTransactionsChain\":2,\"amount\":\"11.720963767319654\",\"hash\":\"13a91a47f63197970f44f6b27626f251db38a2360129b0e41db3f48ae218ef946a9be972bf7efe38423ae086e7655c3bcd865949f4f622b30f808a7beba23663\",\"createTime\":1531408484082}],\"createTime\":1531408484079,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"52ec2b29054ff9936a63ced4b9ba2f1e7243eed6d8268dae583703268a1e4eced833447402a3d5943def88a1a9a03a7f5eaf5bc3e224895357b8760bac673596\",\"senderTrustScore\":94.88296260949774}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"56016a13c66c1a224b7e20d557c8dd3285915ce365a0e91b99b492762f80d936a9c4b370663da1bbe410ecef7d83c1a0ee94c4a1ad61171ba31f4f4e010e470d3994c630\",\"indexInTransactionsChain\":0,\"amount\":\"-4.1950920448619415\",\"hash\":\"36e4065fc10a11bdb8f16c3211cdb580d42c860ae0e48912f0887e2f5d29e1af10572d57c4e20f52d2155fc37d99905777b7430fed02bc18dc7df18521ff03a5\",\"createTime\":1531408484147,\"signatureData\":{\"r\":\"5505c09ccd4153be184aa48f67c06b18f3a34cf0d4fd1d0ad16f4b62fd18fcc0\",\"s\":\"100aa978af6c8af8185cf6b660607747f75e33c38a166b418527cc926c7ff702\"}},{\"addressHash\":\"e157f39d0aa55ec4ff4fd04fa926e0969dba1f47cc57571f5a99a717adf3f6bb9b81259c4af346bd59bc77035922f77fc6ed266c9b007d7e2eb4e56e5dba03a6eceadacf\",\"indexInTransactionsChain\":1,\"amount\":\"-5.047924049957898\",\"hash\":\"757d6679a4c92e6331c26362b086e5357229ab97d3199f1ba97ba517e22a60cf9f5bb7e4f4afc5f5c4c04bc7b7a0d80cbeb52eecc13e2d5b3116dd24bf46af8c\",\"createTime\":1531408484148,\"signatureData\":{\"r\":\"827199e2f57efb1a841d95211a94a56014a91113278975d7789bc74426a24168\",\"s\":\"ca8635e81c5443398c38ab82048a1c06a03243a205bb3eeeaf5e10bcf6eef745\"}},{\"addressHash\":\"1c33aca35656d6d02afd8dcab5876022c502da0db78b372ee193a4293ff5e8d3a9e3f203bc209dfb9b6e3ee1c5c77318862fd2e33603161a4226f71f2599ab492dfb4ad1\",\"indexInTransactionsChain\":2,\"amount\":\"9.2430160948198395\",\"hash\":\"2a3dd63edc16e39c737c9e61b8f86264ba6efe614703cb9a18ac433725a48c4d87dce525c692f8e0b80329a5f18e99570340d76186a0d036d39379bb7bbc7208\",\"createTime\":1531408484148}],\"createTime\":1531408484146,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"1fdd70acbf42662940149a42eed4eb0804f18b05be13c3b0b6f33a49b3910edad50531cefd3b9155500facd4d60e249282a79ad22c4045a86ae366a33dd7444f\",\"senderTrustScore\":57.47265058968483}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"8ad96fcfffceaab17535beca4f33d44a6689ce90e15ec2be27e82ad9da7d59b520dfb7e35350cb1a0685cf2bc0573b9839b102746d134bcf36c779a41b9ea76a3aef6a29\",\"indexInTransactionsChain\":0,\"amount\":\"-8.742517629160126\",\"hash\":\"bc6f87fc717778bf117ef89d0b7c8e52322cffbcd511f3225c0b8356ec52c4ddb0402563919f804af61c519e50209eac898cc31fcf55778c5acf567e8831a694\",\"createTime\":1531408484102,\"signatureData\":{\"r\":\"c03cbbbfaa24a4966e97cb845c41327a8f3350239f29a3f0181bc25919d49d2c\",\"s\":\"77958d8588ae75d17ace2f83ab4ae55898b9f5557010dbf80c4f1e9b256a647f\"}},{\"addressHash\":\"5b5aa17e12a72111fce404e3ca2df38e81a419b43323b449a41cb7b74151d1e0732f7c9d912658500d8abad2e9352c79a5bc7eeb4e26c0cc9474e11e3c87a2b8862d8436\",\"indexInTransactionsChain\":1,\"amount\":\"-8.619187697749151\",\"hash\":\"887a9adbdea558cb469ab2879fcc8e126189bcf223836fd8560049de7f945f21ac746c257660285486e27405d9059bcdf66a7ee4f9bb9efa1743c501bcc172c8\",\"createTime\":1531408484103,\"signatureData\":{\"r\":\"9c1e01c0b93a7e0999169c0cdb02808304d7ce93bff6ab9dba763a628ff7a39f\",\"s\":\"6064e9f74257b93e64e2205f17e645bb2e551abcabc5a17e9f701a81225b721\"}},{\"addressHash\":\"64e1ef09dde2c176696a5378d4eabc3465c3e7e8fd678e77b497652958aabce5fc463031602bc462bcb56a4672350eb97032c326b1ed3b4cf6a6f916e383937439c2e277\",\"indexInTransactionsChain\":2,\"amount\":\"17.361705326909277\",\"hash\":\"1238581b17fb78287191fab752cdbafd8ec3ecda98ec878152dad3d54d37a713565febe9cd2deb2dfae926e9863fba168dace3959b7f6550a1304f9d13093e23\",\"createTime\":1531408484104}],\"createTime\":1531408484101,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"f0f7fff3a2af683dfbec664e8c8a95dcdd97c9ed82237d30168b0f9cdbf78b51e8085cb6ad33e094bf3d75b4c8677a7a60e8efd7097e0f45fe5beaa9fc17ca36\",\"senderTrustScore\":57.01478623492304}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"8ad96fcfffceaab17535beca4f33d44a6689ce90e15ec2be27e82ad9da7d59b520dfb7e35350cb1a0685cf2bc0573b9839b102746d134bcf36c779a41b9ea76a3aef6a29\",\"indexInTransactionsChain\":0,\"amount\":\"-8.169497976120324\",\"hash\":\"ea176059828b3bc1922e2751cd9454cf6bf0c3969d1c9a961554a6d302325f5bc38c9b634f3811080e6e994fae235da6699a37f3f85ae03402b1f11e8f5f9c91\",\"createTime\":1531408484097,\"signatureData\":{\"r\":\"4203879bffe97c05a14e411686159a67a46f727262f252cce584a4b910b95de2\",\"s\":\"2b3d53a8c7cb3d36f3b7bf2e8b3a8d74332f9c6806503f1265e518d8c9e8b170\"}},{\"addressHash\":\"5b5aa17e12a72111fce404e3ca2df38e81a419b43323b449a41cb7b74151d1e0732f7c9d912658500d8abad2e9352c79a5bc7eeb4e26c0cc9474e11e3c87a2b8862d8436\",\"indexInTransactionsChain\":1,\"amount\":\"-6.58805869841925\",\"hash\":\"247640b82e794f3362469211152d9d5ac4ba944eb206750d7d01fc04c655fd29c67cd15041853c0c6010043c4bc24c682c902328743585032cc2f55d516b9f42\",\"createTime\":1531408484099,\"signatureData\":{\"r\":\"71b9953fbf7eda42c21fbd0144c1fa2ca698805e512a6d3db011686893627354\",\"s\":\"4685cedaa2c4aaabad354d1f243db51a93807c5bdfd95fe58252183a2b55cfed\"}},{\"addressHash\":\"4dd77bfc93431784aace419f581ca8bf3d5dde888b87f3629f1e456a92080ca2ba6972edd9cd3f40959918fa3d532d9f705d4e8ba1231b2bfc9d49982bc1186e070d3aff\",\"indexInTransactionsChain\":2,\"amount\":\"14.757556674539574\",\"hash\":\"828273788a73ce1baefd4ade2e29fc1042880c7962cd7354b1d188c80399f976eb425025cf5394e4fabddad101b7d138374613fae9170a1de9ffdab6808c6d33\",\"createTime\":1531408484099}],\"createTime\":1531408484096,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"1582c68e79b7912f30cea6c8181054607900d0aa396fa215422a056a2a63f3ad9812058130ccecaa469bc0bf056f3607ef3d22d700b0ce2e9018db53ccfffa8b\",\"senderTrustScore\":61.16345058749218}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"8ad96fcfffceaab17535beca4f33d44a6689ce90e15ec2be27e82ad9da7d59b520dfb7e35350cb1a0685cf2bc0573b9839b102746d134bcf36c779a41b9ea76a3aef6a29\",\"indexInTransactionsChain\":0,\"amount\":\"-7.857824556009103\",\"hash\":\"bb893e9857e173c1f12044ef930776806c2784218f27c03c98811c5d8778ab286a9b20712e7f2d7e359f917dc45464d364fc08857e22139cd5336ca24adba391\",\"createTime\":1531408484094,\"signatureData\":{\"r\":\"256f9c391ff7aa3996aaa6a231044ae0165da6b9e859d5e7e5f49297d55c974c\",\"s\":\"f55131be052e4edf5235a4be98a4e9b672023fbb739f6de318a817f6ca7541e1\"}},{\"addressHash\":\"5b5aa17e12a72111fce404e3ca2df38e81a419b43323b449a41cb7b74151d1e0732f7c9d912658500d8abad2e9352c79a5bc7eeb4e26c0cc9474e11e3c87a2b8862d8436\",\"indexInTransactionsChain\":1,\"amount\":\"-6.007160013169992\",\"hash\":\"05cee11adca72c34e7d14efcc6608d7f5021944131d5f250d0444c79fbe66a4ba1efade1e777934f59bc641eb815c982b3493ded0e39028aad2c7b42a89e6ba0\",\"createTime\":1531408484096,\"signatureData\":{\"r\":\"3392d542ee0a02dbb1f2dacdc5601469ac08a8a605854a61f59038adbed8f2df\",\"s\":\"6e8ca52eed9fbe5ffa9665a21369867c73be87bcc9857ae3e2f967d9d39d3d4d\"}},{\"addressHash\":\"d7e74e0a136af9fdadc19e8f8a74da21069f3657fca39bfd45542ef387d6254a56919cd20369d8ee844f260fba973f5240360a7357612800788a664ed434b11e33e340d5\",\"indexInTransactionsChain\":2,\"amount\":\"13.864984569179095\",\"hash\":\"66a84545f9f25c50e604e46a16be6cf752b7093d16df51483a719e983915ed027a9af1c1bb7817b1c4835fc59871f4f85eab6f87d4d5af0581850c3c69dfaede\",\"createTime\":1531408484096}],\"createTime\":1531408484093,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"17afb65bb5224d8a18ae321bb0cf62221818d68a7c3174efcc706ea57bfec930b64c43d97fef985f5b28add56d75664f11d9970a07f5f802c46a45b015007576\",\"senderTrustScore\":83.78235642415956}");
        jsons.add("{\"baseTransactions\":[{\"addressHash\":\"8ad96fcfffceaab17535beca4f33d44a6689ce90e15ec2be27e82ad9da7d59b520dfb7e35350cb1a0685cf2bc0573b9839b102746d134bcf36c779a41b9ea76a3aef6a29\",\"indexInTransactionsChain\":0,\"amount\":\"-4.799601085248051\",\"hash\":\"8813b8b01a3df937052d484b5fdc00490b20eb505c74d5395136d656ec9d24bb02e2bdac11bd0cc60e8d73cbf93c7a11cc0186844fea44a7be46bd3bc4af1191\",\"createTime\":1531408484100,\"signatureData\":{\"r\":\"3e5460df8a685de976514c5bd829e642485f5cab8b2c8f274eb57e8b476124c1\",\"s\":\"11efd655f90bfc809c504e4eecfadb190c21f5a407382e585e5a8a4c012f890\"}},{\"addressHash\":\"5b5aa17e12a72111fce404e3ca2df38e81a419b43323b449a41cb7b74151d1e0732f7c9d912658500d8abad2e9352c79a5bc7eeb4e26c0cc9474e11e3c87a2b8862d8436\",\"indexInTransactionsChain\":1,\"amount\":\"-6.389596276680871\",\"hash\":\"21c07b4c9800281c58823448c80c1aac143b9bd23bcc8718f06bdda92771566b3c1fc49e49009ee87c52ce68d56408f666051e48b1a28dd6923f4997946e9866\",\"createTime\":1531408484101,\"signatureData\":{\"r\":\"9377bffe032bc311860d77d6a5fc51879b372bee66c9383a05db8341e003c8d1\",\"s\":\"2a740ac16f5995aae23e4da03b909a7377ba70de7e3671e07319aa353e1f088b\"}},{\"addressHash\":\"ed75bfff199f09c68080fb44f8c984b6bcc5babd3bd91ae584a3c1a69a8b4bfc7c2d43f1cf692eac7d6edb70ee7816dcb2563fb8ee6d14dfe7e5552d764d6eca25feb9ac\",\"indexInTransactionsChain\":2,\"amount\":\"11.189197361928922\",\"hash\":\"717bcb1642da0386058495281d138a3a428f2b844cb1ad3dcd1c53a5e81108131713cf211c968b23f50b2edbf7eefd51680a4ca3286ef88b184222b410fb9bad\",\"createTime\":1531408484101}],\"createTime\":1531408484099,\"baseTransactionsCount\":3,\"transactionDescription\":\"test\",\"hash\":\"46f8cea6c1751f7570380bebf9598d219c7346a776af49de23f7c92a513b132d042cb19a2c37fc32b98e442a14e10e26c23e60fdd8418df1cad2db847ff83694\",\"senderTrustScore\":47.94802011172346}");
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
