import io.coti.fullnode.controllers.TransactionController;
import io.coti.common.model.ConfirmedTransactions;
import io.coti.common.model.UnconfirmedTransactions;
import io.coti.fullnode.AppConfig;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IPropagationService;
import io.coti.common.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j
public class CotiNodeTest {

    private final static String signatureMessage = "message";
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
/*
    @Test
    public void aTestFullProcess() {


        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        List<BaseTransactionData> baseTransactionDataList = createBaseTransactionRandomList(3);

        addTransactionRequest.baseTransactions =baseTransactionDataList;
        addTransactionRequest.hash = new Hash("A1");
        addTransactionRequest.transactionDescription = signatureMessage;
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            propagationService = null;
        });

        ResponseEntity<Response> responseEntity = transactionController.addTransaction(addTransactionRequest);
        Assert.assertTrue(responseEntity.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));


        AddTransactionRequest addTransactionRequest2 = new AddTransactionRequest();
        List<BaseTransactionData> baseTransactionDataList2 = createBaseTransactionRandomList(3);
        addTransactionRequest2.baseTransactions = baseTransactionDataList2;
        addTransactionRequest2.hash = new Hash("A2");
        addTransactionRequest2.transactionDescription = signatureMessage;
        ResponseEntity<Response> responseEntity2 = transactionService.addNewTransaction(addTransactionRequest2);
        Assert.assertTrue(responseEntity2.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity2.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        AddTransactionRequest addTransactionRequest3 = new AddTransactionRequest();
        List<BaseTransactionData> baseTransactionDataList3 = createBaseTransactionRandomList(3);
        addTransactionRequest3.baseTransactions = baseTransactionDataList3;
        addTransactionRequest3.hash = new Hash("A3");
        addTransactionRequest3.transactionDescription = signatureMessage;
        ResponseEntity<Response> responseEntity3 = transactionService.addNewTransaction(addTransactionRequest3);
        Assert.assertTrue(responseEntity3.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity3.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        AddTransactionRequest addTransactionRequest4 = new AddTransactionRequest();
        List<BaseTransactionData> baseTransactionDataList4 = createBaseTransactionRandomList(3);
        addTransactionRequest4.baseTransactions = baseTransactionDataList4;
        addTransactionRequest4.hash = new Hash("A4");
        addTransactionRequest4.transactionDescription = signatureMessage;
        ResponseEntity<Response> responseEntity4 = transactionService.addNewTransaction(addTransactionRequest4);
        Assert.assertTrue(responseEntity4.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity4.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        AddTransactionRequest addTransactionRequest5 = new AddTransactionRequest();
        List<BaseTransactionData> baseTransactionDataList5 = createBaseTransactionRandomList(3);
        addTransactionRequest5.baseTransactions = baseTransactionDataList5;
        addTransactionRequest5.hash = new Hash("A5");
        addTransactionRequest5.transactionDescription = signatureMessage;
        ResponseEntity<Response> responseEntity5 = transactionService.addNewTransaction(addTransactionRequest5);
        Assert.assertTrue(responseEntity5.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity5.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));

        AddTransactionRequest addTransactionRequest6 = new AddTransactionRequest();
        List<BaseTransactionData> baseTransactionDataList6 = createBaseTransactionRandomList(3);
        addTransactionRequest6.baseTransactions = baseTransactionDataList6;
        addTransactionRequest6.hash = new Hash("A6");
        addTransactionRequest6.transactionDescription = signatureMessage;
        TransactionData transactionData6 = new TransactionData(addTransactionRequest.hash, addTransactionRequest.baseTransactions);
        transactionData6.setLeftParentHash(new Hash("A5"));
        addTransactionRequest6.transactionData = transactionData6;

        ResponseEntity<Response> responseEntity6 = transactionService.addPropagatedTransaction(addTransactionRequest6);
        Assert.assertTrue(responseEntity6.getStatusCode().equals(HttpStatus.CREATED));
        Assert.assertTrue(responseEntity6.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));
        try {
            log.info("CotiNodeTest is going to sleep for 20 sec");
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            log.error("error ", e);
        }

        ConfirmationData confirmedData = confirmedTransactions.getByHash(new Hash("A1"));

        ConfirmationData unconfirmedData = unconfirmedTransactions.getByHash(new Hash("A1"));

        Assert.assertNotNull(confirmedData);
        Assert.assertNull(unconfirmedData);

    }

    @Test
    public void bTestBadScenarioNewTransactionNegativeAmount() {
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
    public void cTestBadScenarioNotEnoughSourcesForTcc() {
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
    public void dTestSimpleRollBack() {
        Map<Hash, BigDecimal> preBalanceMap = balanceService.getPreBalanceMap();
        Hash address1 = new Hash("ABCD");
        Hash address2 = new Hash("ABCDEF");

        BaseTransactionData btd1 = new BaseTransactionData(address1, new BigDecimal(5.5));
        BaseTransactionData btd2 = new BaseTransactionData(address2, new BigDecimal(6.57));
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

    private void updateBalancesWithAddressAndAmount(Hash hash, BigDecimal amount) {
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

    private void replaceBalancesWithAmount(Hash hash, BigDecimal amount) {
        Map<Hash, BigDecimal> balanceMap = balanceService.getBalanceMap();
        Map<Hash, BigDecimal> preBalanceMap = balanceService.getPreBalanceMap();

        balanceMap.put(hash, amount);
        preBalanceMap.put(hash, amount);


    }

    private AddTransactionRequest createRequestWithOneBaseTransaction(Hash transactionHash, Hash fromAddress, Hash baseTransactionAddress, BigDecimal amount) {
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();

        BaseTransactionData baseTransactionData =
                new BaseTransactionData(new Hash(CryptoUtils.getPublicKeyFromPrivateKey(BigInteger.valueOf(123)).toByteArray()),
                        amount, baseTransactionAddress,
                        CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(BigInteger.valueOf(123), signatureMessage));


        BaseTransactionData myBaseTransactionData =
                new BaseTransactionData(fromAddress, amount.negate()
                        , new Hash("AB"),
                        CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(BigInteger.valueOf(123), signatureMessage));


        baseTransactionDataList.add(baseTransactionData);
        baseTransactionDataList.add(myBaseTransactionData);


        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        addTransactionRequest.baseTransactions = baseTransactionDataList;
        addTransactionRequest.hash = transactionHash;
        addTransactionRequest.transactionDescription = signatureMessage;
        return addTransactionRequest;
    }


    private List<BaseTransactionData> createBaseTransactionRandomList(int numOfBaseTransactions) {
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
                            CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(privateKey, signatureMessage));

            BaseTransactionData myBaseTransactionData =
                    new BaseTransactionData(myAddress, amount.negate()
                            , myAddress,
                            CryptoUtils.getSignatureStringFromPrivateKeyAndMessage(privateKey, signatureMessage));


            baseTransactionDataList.add(baseTransactionData);
            baseTransactionDataList.add(myBaseTransactionData);

        }
        return baseTransactionDataList;
    }

*/
    }
