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

//        AddTransactionRequest addTransactionRequest6 = new AddTransactionRequest();
//        List<BaseTransactionData> baseTransactionDataList6 = createBaseTransactionRandomList(3);
//        addTransactionRequest6.baseTransactions = baseTransactionDataList6;
//        addTransactionRequest6.hash = new Hash("A6");
//        addTransactionRequest6.transactionDescription = transactionDescription;
//        TransactionData transactionData6 = new TransactionData(addTransactionRequest.baseTransactions,addTransactionRequest.hash,"someDescription", 40 );
//        transactionData6.setLeftParentHash(new Hash("A5"));
//        addTransactionRequest6.transactionData = transactionData6;

//        ResponseEntity<Response> responseEntity6 = transactionService.addPropagatedTransactionFromFullNode(transactionData6);
//        Assert.assertTrue(responseEntity6.getStatusCode().equals(HttpStatus.CREATED));
//        Assert.assertTrue(responseEntity6.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));
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
