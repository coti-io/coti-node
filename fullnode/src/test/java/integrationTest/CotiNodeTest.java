package integrationTest;

import io.coti.basenode.communication.ZeroMQSender;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.basenode.http.AddTransactionRequest;
import io.coti.basenode.model.AddressesTransactionsHistory;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.*;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.fullnode.controllers.TransactionController;
import io.coti.fullnode.services.BalanceService;
import io.coti.fullnode.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TransactionController.class,
        ClusterService.class,
        TransactionHelper.class,
        AddressesTransactionsHistory.class,
        RocksDBConnector.class,
        BalanceService.class,
        LiveViewService.class,
        Transactions.class,
        SourceSelector.class,
        TccConfirmationService.class,
        TransactionIndexes.class,
        DspConsensusCrypto.class,
        TransactionTrustScoreCrypto.class,
        TransactionService.class,
        TransactionCrypto.class,
        ValidationService.class,
        ZeroSpendService.class,
        ZeroMQSender.class,
        TransactionIndexService.class,
        NodeCryptoHelper.class
})
@Slf4j
public class CotiNodeTest {
    List<String> transactionsRequestAsJsons;
    @Autowired
    private TransactionController transactionController;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private BaseNodeBalanceService baseNodeBalanceService;
    @MockBean
    private LiveViewService liveViewService;
    @MockBean
    private io.coti.fullnode.services.WebSocketSender WebSocketSender;
//    @MockBean
//    private org.springframework.messaging.simp.SimpMessagingTemplate SimpMessagingTemplate;
    @MockBean
    private ZeroMQSender ZeroMQSender;

    @Before
    public void init() throws Exception {
        baseNodeBalanceService.init();
        transactionsRequestAsJsons = new ArrayList<String>(
                Arrays.asList("{\"baseTransactions\":[{\"addressHash\":\"138ae277d61cf5dd2d52dc9f71b0f71e66ff80859430c3cb353652b0c264b2e2750257cb713f7eacb1209ca15a0cd40f688aa5da8526ecb4ea688ea9753da501ef18975c\",\"amount\":\"-2.592552663684784\",\"hash\":\"6a547e15ffbd46d7153b47fe3d89254ac28cb8b43a73828168964dbb5ecaf3ac\",\"createTime\":1535374639017,\"signatureData\":{\"r\":\"2fd7e73adf4ea69716b4bbf49bdd2bec728fe5810cc7f349068dfc651b1b847\",\"s\":\"577c7f1d98de8271bec2c34924b6c74378a1175c1676c4447ac525b58b8ab923\"}},{\"addressHash\":\"4831b1320fe931dba6c8ff7648f861f3e6cf0fb38a3499e6c4516d551e0232e471bfd6d803f2d0432adf4a1948995780497a6c467ffb2779cd1ccab52f2d9904c6603128\",\"amount\":\"-2.865889886713445\",\"hash\":\"6318bbacd1c5cffe794803f36ba230e028aff2d817f28062696ef0be57b54a00\",\"createTime\":1535374639018,\"signatureData\":{\"r\":\"7004da0ec3d9faaedaa9494b1537c349165a0a9171bdff4039652cd9ee90cb13\",\"s\":\"6cf920b1a6204e2a7423fd08d0fc67d1a703b686ace09879a1f064031eca6a95\"}},{\"addressHash\":\"2d1e71ba021550f13d7bd78a043fc16a4569e5a1b844fec6b96f9a98feeada1569f8e86f1157d44a654e9e3228aba51a5716ce5976b25543d9470b3fd2351c8c2825de9c\",\"amount\":\"-7.5797959209527095\",\"hash\":\"662d773e3ca8fde65c6a01f638a8f6cd32e6c10d5a9b0a274340fc0a3cbbcc37\",\"createTime\":1535374639018,\"signatureData\":{\"r\":\"3b5b47634792de9889f0f3cb9ce27d51d2dc1f9a7e6f372537212543892efb80\",\"s\":\"6fcffedd2125845541353bf4788eb25ba00dd9618e81183902eab2a42e335545\"}},{\"addressHash\":\"b281116421c606713e01ec812eee5b580e27a7313e97b8c5a1cc72ca43e28eb374b38e12eca33488c298ccbcdd976c352d1b53b009352a3a100911757495f6a55e7bcd0e\",\"amount\":\"13.0382384713509385\",\"hash\":\"b5e182a5d175cde38a3b10f46d6755d8b7320111f62c19eec2d8a7c1915f9623\",\"createTime\":1535374639018}],\"createTime\":1535374639016,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"046fd3e1ef270becfeb6f87281fe47f966cc1b133ab0c341d56e4d6e96eed0cd1d6827f2728470ef2eea6be446cff8fa8d175683acaf6a04b18aed4c4dd3fddd31\",\"transactionHash\":\"56236ae7d17e7f0bf1888d45dfb9cfb7648c15549aef9b79e360050a66388d21\",\"trustScore\":78.63868,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"47b794934c2017c67a5891974d76497c6540d96ca4ece89f859ab3f588019521\",\"s\":\"446d103c236bf3e8430fd76031a9197aa3ea7952bd60c4c486741cb588fe8453\"}}],\"senderHash\":\"046fd3e1ef270becfeb6f87281fe47f966cc1b133ab0c341d56e4d6e96eed0cd1d6827f2728470ef2eea6be446cff8fa8d175683acaf6a04b18aed4c4dd3fddd31\",\"hash\":\"56236ae7d17e7f0bf1888d45dfb9cfb7648c15549aef9b79e360050a66388d21\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"138ae277d61cf5dd2d52dc9f71b0f71e66ff80859430c3cb353652b0c264b2e2750257cb713f7eacb1209ca15a0cd40f688aa5da8526ecb4ea688ea9753da501ef18975c\",\"amount\":\"-4.130713521950383\",\"hash\":\"4d2b561037c7486565be1ac1966a893538814d2b12067bb791ef4bee2b54fca8\",\"createTime\":1535374639020,\"signatureData\":{\"r\":\"e4e0e10fb95515e3d98a957b7612434390b76774aae42f893de6b4a6120deeb3\",\"s\":\"fc5a89f0b190da9dab8a14468c5b9626b35d2786ee3841af02647bdfa6d3cc09\"}},{\"addressHash\":\"4831b1320fe931dba6c8ff7648f861f3e6cf0fb38a3499e6c4516d551e0232e471bfd6d803f2d0432adf4a1948995780497a6c467ffb2779cd1ccab52f2d9904c6603128\",\"amount\":\"-7.948099873539729\",\"hash\":\"9cab221c5ed09a5e741d50214c18d27c94041a9b3b1d6f2a9956d90f365c3805\",\"createTime\":1535374639021,\"signatureData\":{\"r\":\"d2ed8b4bb1d9b4a1e282eb66de83483d0c6924560a9ac69cf2b217b2710b9bda\",\"s\":\"bb9229e44ea55e9767bfbf329ed37d2f8b5a4ab468cc0b65d41c6d16340db206\"}},{\"addressHash\":\"2d1e71ba021550f13d7bd78a043fc16a4569e5a1b844fec6b96f9a98feeada1569f8e86f1157d44a654e9e3228aba51a5716ce5976b25543d9470b3fd2351c8c2825de9c\",\"amount\":\"-7.731424919267499\",\"hash\":\"fd6f1ef646b0a234dde274be085e91560b707158a681c84bf24bf58779cd7353\",\"createTime\":1535374639021,\"signatureData\":{\"r\":\"17fcad96010a6167188fd14c2d28267e48a6c42e146eb4cea0a910a6499ccb26\",\"s\":\"515a3b83a549a6dc2a24c59cd7d6145af37aa48d4bb049adf5fc1de3897b06c0\"}},{\"addressHash\":\"6033614d8e9047122a7f6686ec6dcafd6bfdd578f6c2f3530e3188860f560876920007947d0a60c0e88265733ba03e86eb0788ee30288c28b5b7be041957cd8eaeff7a52\",\"amount\":\"19.810238314757611\",\"hash\":\"9eee1c8289be8bf9cb1b471a47af520ff5d343613904f924b0f07af108b6bc3c\",\"createTime\":1535374639021}],\"createTime\":1535374639019,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"046fd3e1ef270becfeb6f87281fe47f966cc1b133ab0c341d56e4d6e96eed0cd1d6827f2728470ef2eea6be446cff8fa8d175683acaf6a04b18aed4c4dd3fddd31\",\"transactionHash\":\"ca80c795717e140dcba72634a019e28c0a6fb108f0a37b8132a494ff95856807\",\"trustScore\":78.63868,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"c08a96325d1e1d2ac25473675e0806a59c6074afef6da79cb0d3dfda1b58afc2\",\"s\":\"9bea2d6714e3e4e42e1fbd0e7e6d4a0e250d20d509b5708701f7e2ffe89dc288\"}}],\"senderHash\":\"046fd3e1ef270becfeb6f87281fe47f966cc1b133ab0c341d56e4d6e96eed0cd1d6827f2728470ef2eea6be446cff8fa8d175683acaf6a04b18aed4c4dd3fddd31\",\"hash\":\"ca80c795717e140dcba72634a019e28c0a6fb108f0a37b8132a494ff95856807\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"0538a56250d0760be86e825e24861ff065bac8b5c2444f757c2551aef72f1395fe398e86dfbc1ec4df8431a2d89c273d7139f502f065b61cbfff749dbb5a894a45ec70a8\",\"amount\":\"-6.6242297072293645\",\"hash\":\"f0d065d37c531bf53e23c1c27484c6b7786d5fa8e7843c4c454f0e61ec2a51ac\",\"createTime\":1535374645738,\"signatureData\":{\"r\":\"ccf8aabd323569f78fd386a359fd5e9c46d00fccaf876423e833b533c24da208\",\"s\":\"bf94b15821ae84d3c3821035cd6ade6bb9c2badbcfbe9a2f8deef171979a2fab\"}},{\"addressHash\":\"9d08a489e4701c2322e08378209cdfac4802e8790771afdd525956bdd5ca046d6622b0e3927b1e40e41cef11aac2ed409debbe456cd571cbc807676609681398aefbded0\",\"amount\":\"-3.3690517147879335\",\"hash\":\"a090b332149e248baff5f3bbf4f0fdddf3dc0e282cf7915779c6a044a4fa36af\",\"createTime\":1535374645738,\"signatureData\":{\"r\":\"f41209f84f7ead7b2951f17276b381b1e46504732d9d043b60ef3a321a3be206\",\"s\":\"fc6e1bc4029e82d0bc670a7b76be8d43c4a6f3cb44aec648a9e7025df27f2f92\"}},{\"addressHash\":\"0b8ada11e3bc342c85a8527d9f540141ccbe5ecade41ed79d3fdb39db02c84ae18bb8baf7a51324b2c50b3240cb9dcd87bc8054f1e39650d2db54fbc27b0bccaffd98d74\",\"amount\":\"-10.404282142350354\",\"hash\":\"2e20cfa154e6810394463302881a53b7b4d3d94e5ec25c40f345856a1992dd41\",\"createTime\":1535374645739,\"signatureData\":{\"r\":\"b9c801db0005619c8f34322e836c728a73b1556a122bf354e41ce7c486b57c12\",\"s\":\"4129098267ad9fd1c20225ef5aa2a499a39b5da0768f1a1d2d059e75f70b6ee8\"}},{\"addressHash\":\"2124c83f83feba14bdf56ea44ff7098a1c6e122c9dd9dcefe98f199c14262a462c39f09a38bcf48d62522d9b631211aad571b91e0d527e1be410cafa2d41688254279667\",\"amount\":\"20.3975635643676520\",\"hash\":\"6df29301da7b8f85245fd1a510f191b337b0ca3381cfb26bbc705d21ce4cc82e\",\"createTime\":1535374645739}],\"createTime\":1535374645737,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04b9a8be7bc2e5eb52e5dad15b91c56446c99803b424f5cf47f1adb32852950d67daa9cbf2da17cf797ca1c85f2d9b8d32070db60ef01a430011a12f80be58d576\",\"transactionHash\":\"63da19b0a7f04b8e5df01e102fbffa556a63e0a5356cdfa8417268ca78383b5e\",\"trustScore\":95.52162,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"9c20fa76768be47f34301ad2a3cf292367127096e2fe32c6367e34a549dff608\",\"s\":\"3372052538eae6e4d9c0bc5c661adc12d97fc5411710750e46ce2689c9c7e6a5\"}}],\"senderHash\":\"04b9a8be7bc2e5eb52e5dad15b91c56446c99803b424f5cf47f1adb32852950d67daa9cbf2da17cf797ca1c85f2d9b8d32070db60ef01a430011a12f80be58d576\",\"hash\":\"63da19b0a7f04b8e5df01e102fbffa556a63e0a5356cdfa8417268ca78383b5e\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"be62b8b7c92a111da8851f276b48f42a3be9cc7f12f8d48bc6a6505ff7b7009b821d0c36f60606697bdde82b9bd150f5bb3d3010646498970d96218e8c5e2d7dd7585690\",\"amount\":\"-2.986800569991578\",\"hash\":\"3cd1d22cb09518025f8be549094ec3c069a40c71ede1dc536bd3494033ff9843\",\"createTime\":1535374636541,\"signatureData\":{\"r\":\"95771f5104eac1bb513fab50c47b3442cc3e3da9f79873edd24e6de9722538d1\",\"s\":\"e048113f2506f3e7e103cf8549796640ec4b7761ccaaeea68b08f446316d25a7\"}},{\"addressHash\":\"5e6b6af708ae15c1c55641f9e87e71f5cd58fc71aa58ae55abe9d5aa88b2ad3c5295cbffcfbb3a087e8da72596d7c60eebb4c59748cc1906b2aa67be43ec3eb147c1a19a\",\"amount\":\"-4.719943468440176\",\"hash\":\"787d575d074ffadd777cab9f2177d0190fbbd24d5e56d39772b1da3c189c2489\",\"createTime\":1535374636542,\"signatureData\":{\"r\":\"e91c773786405162f6b316f42c4c0e5b09a451dddd11886b78a4fd85aa3afaa1\",\"s\":\"ac981f1469fe4459a6ed9ff3889c69b1b28994d8d3c03daacc77c95c0b35fbb5\"}},{\"addressHash\":\"e242de04b23252249bf0460e4f9f2c22aa464e1fd09ebb4599c124e0035961203b7be0bc1e27f6811452a4627a62225db02d13dab4af927f0a9a7852125200129711311d\",\"amount\":\"-5.910721109301685\",\"hash\":\"9eaa396caa7173f79955a6d8eeb1956e49347790c1cf47585f65c4a6cfa47ae3\",\"createTime\":1535374636543,\"signatureData\":{\"r\":\"45587c0f233152060a1bb6429820a90f6373f130f9da6240c999e6bd5534b966\",\"s\":\"f4a749570026c865f885a3340c6b5209e5715083b02fe096c251dfb85d1689ef\"}},{\"addressHash\":\"be75e8b1b436c252bf698627cc249f4d8a97c5ae9e459582a5b99cb58f811271e1f1a9c4bb6c1d0024f78e41f0709b32e27120696fab4d3ea241a16090f8aaab4d94fe73\",\"amount\":\"13.617465147733439\",\"hash\":\"5c2a7bb0f891099526a105a9cc49e45bc204111ab82234632c8ab64e78e58138\",\"createTime\":1535374636543}],\"createTime\":1535374636540,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"042d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702\",\"transactionHash\":\"8d66f25ccf3693f46bf87a57174a61404645a1581e366645b45af35f280ad0f7\",\"trustScore\":40.79462,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"63d45ff887ebae5238b7173fa8480114e569b064d115cf2bd1a638217c2bc895\",\"s\":\"d6f9a8009bba0eb8f4a5cfe310083f75393cc7bba808adcbb6e9b3660e4d234\"}}],\"senderHash\":\"042d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702\",\"hash\":\"8d66f25ccf3693f46bf87a57174a61404645a1581e366645b45af35f280ad0f7\"}",
                        // Duplicated transaction
                        "{\"baseTransactions\":[{\"addressHash\":\"be62b8b7c92a111da8851f276b48f42a3be9cc7f12f8d48bc6a6505ff7b7009b821d0c36f60606697bdde82b9bd150f5bb3d3010646498970d96218e8c5e2d7dd7585690\",\"amount\":\"-2.986800569991578\",\"hash\":\"3cd1d22cb09518025f8be549094ec3c069a40c71ede1dc536bd3494033ff9843\",\"createTime\":1535374636541,\"signatureData\":{\"r\":\"95771f5104eac1bb513fab50c47b3442cc3e3da9f79873edd24e6de9722538d1\",\"s\":\"e048113f2506f3e7e103cf8549796640ec4b7761ccaaeea68b08f446316d25a7\"}},{\"addressHash\":\"5e6b6af708ae15c1c55641f9e87e71f5cd58fc71aa58ae55abe9d5aa88b2ad3c5295cbffcfbb3a087e8da72596d7c60eebb4c59748cc1906b2aa67be43ec3eb147c1a19a\",\"amount\":\"-4.719943468440176\",\"hash\":\"787d575d074ffadd777cab9f2177d0190fbbd24d5e56d39772b1da3c189c2489\",\"createTime\":1535374636542,\"signatureData\":{\"r\":\"e91c773786405162f6b316f42c4c0e5b09a451dddd11886b78a4fd85aa3afaa1\",\"s\":\"ac981f1469fe4459a6ed9ff3889c69b1b28994d8d3c03daacc77c95c0b35fbb5\"}},{\"addressHash\":\"e242de04b23252249bf0460e4f9f2c22aa464e1fd09ebb4599c124e0035961203b7be0bc1e27f6811452a4627a62225db02d13dab4af927f0a9a7852125200129711311d\",\"amount\":\"-5.910721109301685\",\"hash\":\"9eaa396caa7173f79955a6d8eeb1956e49347790c1cf47585f65c4a6cfa47ae3\",\"createTime\":1535374636543,\"signatureData\":{\"r\":\"45587c0f233152060a1bb6429820a90f6373f130f9da6240c999e6bd5534b966\",\"s\":\"f4a749570026c865f885a3340c6b5209e5715083b02fe096c251dfb85d1689ef\"}},{\"addressHash\":\"be75e8b1b436c252bf698627cc249f4d8a97c5ae9e459582a5b99cb58f811271e1f1a9c4bb6c1d0024f78e41f0709b32e27120696fab4d3ea241a16090f8aaab4d94fe73\",\"amount\":\"13.617465147733439\",\"hash\":\"5c2a7bb0f891099526a105a9cc49e45bc204111ab82234632c8ab64e78e58138\",\"createTime\":1535374636543}],\"createTime\":1535374636540,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"042d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702\",\"transactionHash\":\"8d66f25ccf3693f46bf87a57174a61404645a1581e366645b45af35f280ad0f7\",\"trustScore\":40.79462,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"63d45ff887ebae5238b7173fa8480114e569b064d115cf2bd1a638217c2bc895\",\"s\":\"d6f9a8009bba0eb8f4a5cfe310083f75393cc7bba808adcbb6e9b3660e4d234\"}}],\"senderHash\":\"042d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702\",\"hash\":\"8d66f25ccf3693f46bf87a57174a61404645a1581e366645b45af35f280ad0f7\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"fc3085704201a5bfed92a507c00023c2e5cb6045c9faeb4bd7c7b2da5a021b6b6d472ab2b6ce5528eee5f2afec91fdf0bfcd583432089357cad1b9342760b08603878703\",\"amount\":\"-4.012520232218711\",\"hash\":\"48df686eff4ac5302d311e378bf97983885b58eed01760b681b8646631f7a94f\",\"createTime\":1535374644710,\"signatureData\":{\"r\":\"2a85041e8a383e4fef48201cf1c1c7d8ee8310bbd867ce92f591871423ef4841\",\"s\":\"5b31429d14ac272278637785a4f9874fa704fe620aa54695aca8d86ffec33ade\"}},{\"addressHash\":\"dd439ed847fb439e2e7768c4a0f4929d2ed5b4f4a7df6e7a31d39919fa79e17a6bc63961a89fcdeeb9ef012b828d48487df829d4bc140a0b81a176deef2acfd238db364a\",\"amount\":\"-7.378033794184585\",\"hash\":\"cbd44813a7136a1ff4e0c0018f3954a5b45f68853284643250c20c3acf5ff1b7\",\"createTime\":1535374644711,\"signatureData\":{\"r\":\"613cf4ecbd9d09721960a96ab0a4af95e26b61a06234f88efd18187862e74881\",\"s\":\"868b28f798e40ef63d934f89387ca99911fe7e75962f88c5b5bea8edfe90ba6d\"}},{\"addressHash\":\"6723bd3b36034f33319c82788ba85ee6c5dd7787eaf994eb01de5ac4501ea1dcf7b6e2cde0b4b2348d488f1a9d0e76c8901ac8e59c6972272996cc9aecd883e42efb8c10\",\"amount\":\"-4.642429668167703\",\"hash\":\"05b698f411fd00e8bd3cc0513cc6444e0a9ab3c020420d0953b96eb9ccb0cd60\",\"createTime\":1535374644712,\"signatureData\":{\"r\":\"60223f54ecdfacd7f660db69ab9ecd5b9f721a1fc53b964e258aa809644af4a5\",\"s\":\"4abd3036e9ac290a6e9448bb2f8299611255f9987861bc5d123738195bd996cc\"}},{\"addressHash\":\"6a3702fb618eb1b9d744bb48f412dd3f0ed991178cf8ea39cdf1f41f4e4c6c3552021bd7a523393717bb9fd36af5c1920ba13f220fa79f74dcbde806ea338f911548e6e8\",\"amount\":\"16.032983694570999\",\"hash\":\"519758647a6a12dfe3e49e255bdbe1d57c5dfb4e57ba01434dfd029525397468\",\"createTime\":1535374644712}],\"createTime\":1535374644710,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"0467b0c92ddecfb5b029f4801a8213fdbb145a9661bbbed4f3985d9557f7375616483651aa43e1acab4870900aaf82fad2c11a99fe91b960bb0b0794003254e206\",\"transactionHash\":\"1ffdbbb42452d97d58fce56ba7ac4914cf2c0295376f2304d2afca308463d267\",\"trustScore\":74.52674,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"fd83ff47c26a7fd5aa5fc5b2e1baf123c8f6d3140135303c5fa0e9ee4664289a\",\"s\":\"576fcf13fd5caa31e0ad9177ff3d1b267040898b405570254b8dd4cf83b5b697\"}}],\"senderHash\":\"0467b0c92ddecfb5b029f4801a8213fdbb145a9661bbbed4f3985d9557f7375616483651aa43e1acab4870900aaf82fad2c11a99fe91b960bb0b0794003254e206\",\"hash\":\"1ffdbbb42452d97d58fce56ba7ac4914cf2c0295376f2304d2afca308463d267\"}",
                        // Not valid addressHash
                        "{\"baseTransactions\":[{\"addressHash\":\"99a27f6d2e788cd857a454e5d83ff3358bfef1ff6de25b1d56b8eb48f02185d091f57f7946dee409b19aef29c1ac71dd69ab55d0a2f3aa2177edf1074ca2d3d8c3572f87\",\"amount\":\"-2.4131335714288853\",\"hash\":\"8aaf6c4b47974a6bc685c459f8b5908e96c0bd484af322bcfeb2a99777629f24\",\"createTime\":1535374640236,\"signatureData\":{\"r\":\"90a55d263e6e993424d51c08c77392c355c603528cd902e6c6f0ee707905ac86\",\"s\":\"3ab5703d5c61d0c398be006e06c01ad2a5152765604629c2a4d96c49f3dacca0\"}},{\"addressHash\":\"4cfdaa8bc34de1a736ee2d56645e8d2677d7f48fe90342c79eaf8f23e0eec8a48b85552c90a77dcca8fc5752ce2fc686985321d9f00820060b9df5f67200f58e75ec4971\",\"amount\":\"-9.133828657257588\",\"hash\":\"f1875df2a5da88013ecf73aca697388090194ef88e2c4de63684898374c304e7\",\"createTime\":1535374640237,\"signatureData\":{\"r\":\"4ebe70454644ff6c9686fef2e08b614ca6613fe862a176f938eeac9ff6604e2d\",\"s\":\"f0e4fa7139cbb2262eb7cbdfb7c4baf40bf7e759cddd5edb3260ca98de605ea9\"}},{\"addressHash\":\"14ef167df79df633d4c78a72eab79fe46aa6ae08044331e0cb5f868ee57a62cbd95ddf5754ae1306327f0c10d3d7fabde6c66f225dbab34df334c3e54d4d3db902b18993\",\"amount\":\"-7.666255741491952\",\"hash\":\"6dc132020fe64152324802b06a92e3a8e5bfaf7d6a4fdf5c138f5cc823bb3b81\",\"createTime\":1535374640238,\"signatureData\":{\"r\":\"f8746c5f300eb0616c244cee1d8d3a06cea541fb0cf16314e80fd28eb8b8faf4\",\"s\":\"1057891e75dc0b757a6d60a477fc80e26fcf6fdd2dba26d6f61ff1472a44d6b2\"}},{\"addressHash\":\"684e0a4cd14263fd4d97f03a0572edfb16c095fb558b661a2f1c5c0a83b49a9e03a8b4dafea6efd8cab18a23ffc3a5bc8f93317e501defc0cf5ff161542e6ddc2f5ef9e3\",\"amount\":\"19.2132179701784253\",\"hash\":\"a188ffb5ad203eb6c6312a700020e36e4bffc6b6c8fe9cb064e8eb442be36027\",\"createTime\":1535374640238}],\"createTime\":1535374640236,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04dd6893b9d67731721f409ffb782b88be747d2e409ce8138a808daabe77f2693637aefffdb7b61aabc85bd1a194327923a52b1a99387d0adae6c645055370fe16\",\"transactionHash\":\"9afb86622e8018ed8d4e9166bef38486e8df9d53529e0e6b9f70260bc72d455e\",\"trustScore\":77.67813,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"9d600c09285220ccbacb7d83c231757e007f546332fd64f6d8e31e4259ee32d1\",\"s\":\"10eac4edf5e56b73cbfab0da04af0b3d6e39568bc699ac1f644b1c76318a66e2\"}}],\"senderHash\":\"04dd6893b9d67731721f409ffb782b88be747d2e409ce8138a808daabe77f2693637aefffdb7b61aabc85bd1a194327923a52b1a99387d0adae6c645055370fe16\",\"hash\":\"9afb86622e8018ed8d4e9166bef38486e8df9d53529e0e6b9f70260bc72d455e\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"87a27f6d2e788cd857a454e5d83ff3358bfef1ff6de25b1d56b8eb48f02185d091f57f7946dee409b19aef29c1ac71dd69ab55d0a2f3aa2177edf1074ca2d3d8c3572f87\",\"amount\":\"-2.4131335714288853\",\"hash\":\"8aaf6c4b47974a6bc685c459f8b5908e96c0bd484af322bcfeb2a99777629f24\",\"createTime\":1535374640236,\"signatureData\":{\"r\":\"90a55d263e6e993424d51c08c77392c355c603528cd902e6c6f0ee707905ac86\",\"s\":\"3ab5703d5c61d0c398be006e06c01ad2a5152765604629c2a4d96c49f3dacca0\"}},{\"addressHash\":\"4cfdaa8bc34de1a736ee2d56645e8d2677d7f48fe90342c79eaf8f23e0eec8a48b85552c90a77dcca8fc5752ce2fc686985321d9f00820060b9df5f67200f58e75ec4971\",\"amount\":\"-9.133828657257588\",\"hash\":\"f1875df2a5da88013ecf73aca697388090194ef88e2c4de63684898374c304e7\",\"createTime\":1535374640237,\"signatureData\":{\"r\":\"4ebe70454644ff6c9686fef2e08b614ca6613fe862a176f938eeac9ff6604e2d\",\"s\":\"f0e4fa7139cbb2262eb7cbdfb7c4baf40bf7e759cddd5edb3260ca98de605ea9\"}},{\"addressHash\":\"14ef167df79df633d4c78a72eab79fe46aa6ae08044331e0cb5f868ee57a62cbd95ddf5754ae1306327f0c10d3d7fabde6c66f225dbab34df334c3e54d4d3db902b18993\",\"amount\":\"-7.666255741491952\",\"hash\":\"6dc132020fe64152324802b06a92e3a8e5bfaf7d6a4fdf5c138f5cc823bb3b81\",\"createTime\":1535374640238,\"signatureData\":{\"r\":\"f8746c5f300eb0616c244cee1d8d3a06cea541fb0cf16314e80fd28eb8b8faf4\",\"s\":\"1057891e75dc0b757a6d60a477fc80e26fcf6fdd2dba26d6f61ff1472a44d6b2\"}},{\"addressHash\":\"684e0a4cd14263fd4d97f03a0572edfb16c095fb558b661a2f1c5c0a83b49a9e03a8b4dafea6efd8cab18a23ffc3a5bc8f93317e501defc0cf5ff161542e6ddc2f5ef9e3\",\"amount\":\"19.2132179701784253\",\"hash\":\"a188ffb5ad203eb6c6312a700020e36e4bffc6b6c8fe9cb064e8eb442be36027\",\"createTime\":1535374640238}],\"createTime\":1535374640236,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04dd6893b9d67731721f409ffb782b88be747d2e409ce8138a808daabe77f2693637aefffdb7b61aabc85bd1a194327923a52b1a99387d0adae6c645055370fe16\",\"transactionHash\":\"9afb86622e8018ed8d4e9166bef38486e8df9d53529e0e6b9f70260bc72d455e\",\"trustScore\":77.67813,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"9d600c09285220ccbacb7d83c231757e007f546332fd64f6d8e31e4259ee32d1\",\"s\":\"10eac4edf5e56b73cbfab0da04af0b3d6e39568bc699ac1f644b1c76318a66e2\"}}],\"senderHash\":\"04dd6893b9d67731721f409ffb782b88be747d2e409ce8138a808daabe77f2693637aefffdb7b61aabc85bd1a194327923a52b1a99387d0adae6c645055370fe16\",\"hash\":\"9afb86622e8018ed8d4e9166bef38486e8df9d53529e0e6b9f70260bc72d455e\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"dcd2189eebb94080038cd6d4a96d2a9b0e84183f1e18b3f8987ef3a80a1cdd1f968f0a68136773cba1190616452a508c90ca02e10844fc8f3071fb8156e20cf165174f4d\",\"amount\":\"-7.56981987357704\",\"hash\":\"c44574cbfb9bcb5b5dc64ed3f7a867b29e3f90197420b48c43ffe28e17f4e6ab\",\"createTime\":1535374638646,\"signatureData\":{\"r\":\"83ab843a889a5340209113563cd942ca2a4f856a2eb6e019eaf98c489ed241a0\",\"s\":\"de512e9fd90729ee700ec9cc8d8f2a21d6a1a8d183a3a9202555426d794d8b39\"}},{\"addressHash\":\"031a6c51685511b6dd468d670600afa9e8cda88915be9d7e8449c905545c1f141b9e02bbf77469f4788a8481a53fb5d2a2d21920715555c1845c315a30de02ee34762f62\",\"amount\":\"-4.6705174186888465\",\"hash\":\"ab4cf057efa5b21173c244f555169ee2ce4813a66ea73a4f49925392e535ccfa\",\"createTime\":1535374638647,\"signatureData\":{\"r\":\"700b160b57e711d57a90c0834184c0c7fa9754c3916cac059e135957feeb5688\",\"s\":\"eff2feaded2a9c3d14df7e67e50f7247da99e695808c4e6e560e937a23c0e2bf\"}},{\"addressHash\":\"cf95fe9b41f6802affe4174c757bc499ed53c6290b1fc9949ff0dff640c3228f517b29a092fc07a0d11c29806f1bde6a2626df1239f2ef58d8abf93b4f39c86dfe8244cd\",\"amount\":\"-3.8926402856052844\",\"hash\":\"8f4ed6f858255d8c122ddc3c92449244583a7d602d775d2ce74342423a120b3e\",\"createTime\":1535374638647,\"signatureData\":{\"r\":\"5905cbbe429d121c3b501ec367622e5cfce195fe98b7df1bd7d63e544650d81\",\"s\":\"c09bae20d4bd9ce4cb949315984dd06ed39a6c2859a18667c2e9ba5793e67c12\"}},{\"addressHash\":\"e80fd82743a01dd76a8dc145342d351a6fc0f66794567d9499bc179e413a689765ed28c0c0ef5b66395ad689ca41de3adcc80196999bde95042a9cad87e5ef6111ada8c2\",\"amount\":\"16.1329775778711709\",\"hash\":\"aa0defe3fd415c4df5b44620f8c6d37349275b9f346802e79a648ebca387b177\",\"createTime\":1535374638647}],\"createTime\":1535374638645,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04814e8aade05fe0a908644bc5a78ff71e302ec8807f9ad26680245c5f16ae7163ffd9612d0a75d79d2693c95b6a2fa82690967a3a6eb7e165d5880c14703f520d\",\"transactionHash\":\"72b299f57f1691d0ab3da73f142ecb03eb48e835ea535cbece5cd71dd1b557fe\",\"trustScore\":52.59905,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"eb33b85b526660a8e6eab8a21bb4d0fb2f8709400712fcca3dbfb5ebf55abad7\",\"s\":\"cbf340a085e0d4a00e83578346de5868703c308b4ca2ed9b997ef1b4c42aa9ad\"}}],\"senderHash\":\"04814e8aade05fe0a908644bc5a78ff71e302ec8807f9ad26680245c5f16ae7163ffd9612d0a75d79d2693c95b6a2fa82690967a3a6eb7e165d5880c14703f520d\",\"hash\":\"72b299f57f1691d0ab3da73f142ecb03eb48e835ea535cbece5cd71dd1b557fe\"}",
                        // Not valid baseTransaction Hash
                        "{\"baseTransactions\":[{\"addressHash\":\"7b979f3ec14f082c1c1777a34035765dd3eae5d9ab7f8b1ed3d733de46f6a63f3d92a40f71071d9418073b5aa50d6af662f79e092b03253a4650c05e4dc90cded411014d\",\"amount\":\"-8.806263480485944\",\"hash\":\"1fd8606e74241dbc0011aa589fb7c9c69032fb7e9c2315fc289e0a4d54116ca7\",\"createTime\":1535374641327,\"signatureData\":{\"r\":\"f46825e38b89f952de262b724db37e155500506168a9e3edae1ba4cb17b5d6e8\",\"s\":\"8936e55440820d28bb01cdae856a7f6bb5346c01bb4ce37fae3dfc42f7cacb85\"}},{\"addressHash\":\"dd6341294a73960e7ad0f040c43b587c0d6a313e271f4ebb405995a790c23d4172c015949e35f302fc68ba3e942db241797b0520d3ba8b182e7d2e2642bc32b7a816c0fb\",\"amount\":\"-8.266961186003487\",\"hash\":\"7f50af5cbeab68201c4d77d0d46f53b5c3ced26d98054a9a3da13aee2e49a748\",\"createTime\":1535374641327,\"signatureData\":{\"r\":\"5b6e5fb9c4640881d8a905a347a80883d6c1203277c401cd09379af6e9b6d5f2\",\"s\":\"bbc0e73926b6810e79c82ef6cc6ad3c9db00eaac2cf5996e7b1b112bc53fefdc\"}},{\"addressHash\":\"49fe9a2c48e53ef93465fdecb1722d67b9123358b993bd9b7f6a40f9299aae466f206d7989a61f6b4175b7715e5f91b027fe7ad117c6188f072661f2284003f194ce9652\",\"amount\":\"-6.770701851502806\",\"hash\":\"1da0e8ee4e404173f149266375f0b6eb4f2acf4d2f7c0fe1b72830f0689c4b45\",\"createTime\":1535374641328,\"signatureData\":{\"r\":\"d5a43561f5cc9866c47508fc832f1c3ffff4b617f133e33a74b11079fcfcbe7f\",\"s\":\"692ec9088224b931467a536922a6816e5c190b8b0927dca34539fe92e332f730\"}},{\"addressHash\":\"de03d69baeb08c8c6b9a3f65a8c42fe47616ac3762d0a68a6ae2484fe2b3f57d464170914d6daee3fe0de446be402bb58fd2140e6bc3b3e2c1d2ed274b7b4d575e43c13f\",\"amount\":\"23.843926517992237\",\"hash\":\"e891acc744073c8826f2e5bd6a402a5dceeb3b0d4c0257185bb2bc9e9860c02f\",\"createTime\":1535374641328}],\"createTime\":1535374641326,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04bf8bf270293886641e8ff9db722e2dfcb255f3d9464a0fc135e28ad01db9dca0ec5f4b70bb0b980774eaa410350b355da932c0827c334242eb0924c025a954b4\",\"transactionHash\":\"e316642496988b924c3dbe9794f822a507ea2e0883e96808a1c02725c41c1c8d\",\"trustScore\":60.14619,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"94a2292c6770e9641186fdf181225dfceed02728d9a88085a0d2d02c23f538bb\",\"s\":\"c36cf333e58d9a5abeaf4cb72683045cccc29decfc7036c4a2c2274f9e1d75a2\"}}],\"senderHash\":\"04bf8bf270293886641e8ff9db722e2dfcb255f3d9464a0fc135e28ad01db9dca0ec5f4b70bb0b980774eaa410350b355da932c0827c334242eb0924c025a954b4\",\"hash\":\"e316642496988b924c3dbe9794f822a507ea2e0883e96808a1c02725c41c1c99\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"7b979f3ec14f082c1c1777a34035765dd3eae5d9ab7f8b1ed3d733de46f6a63f3d92a40f71071d9418073b5aa50d6af662f79e092b03253a4650c05e4dc90cded411014d\",\"amount\":\"-8.806263480485944\",\"hash\":\"1fd8606e74241dbc0011aa589fb7c9c69032fb7e9c2315fc289e0a4d54116ca7\",\"createTime\":1535374641327,\"signatureData\":{\"r\":\"f46825e38b89f952de262b724db37e155500506168a9e3edae1ba4cb17b5d6e8\",\"s\":\"8936e55440820d28bb01cdae856a7f6bb5346c01bb4ce37fae3dfc42f7cacb85\"}},{\"addressHash\":\"dd6341294a73960e7ad0f040c43b587c0d6a313e271f4ebb405995a790c23d4172c015949e35f302fc68ba3e942db241797b0520d3ba8b182e7d2e2642bc32b7a816c0fb\",\"amount\":\"-8.266961186003487\",\"hash\":\"7f50af5cbeab68201c4d77d0d46f53b5c3ced26d98054a9a3da13aee2e49a748\",\"createTime\":1535374641327,\"signatureData\":{\"r\":\"5b6e5fb9c4640881d8a905a347a80883d6c1203277c401cd09379af6e9b6d5f2\",\"s\":\"bbc0e73926b6810e79c82ef6cc6ad3c9db00eaac2cf5996e7b1b112bc53fefdc\"}},{\"addressHash\":\"49fe9a2c48e53ef93465fdecb1722d67b9123358b993bd9b7f6a40f9299aae466f206d7989a61f6b4175b7715e5f91b027fe7ad117c6188f072661f2284003f194ce9652\",\"amount\":\"-6.770701851502806\",\"hash\":\"1da0e8ee4e404173f149266375f0b6eb4f2acf4d2f7c0fe1b72830f0689c4b45\",\"createTime\":1535374641328,\"signatureData\":{\"r\":\"d5a43561f5cc9866c47508fc832f1c3ffff4b617f133e33a74b11079fcfcbe7f\",\"s\":\"692ec9088224b931467a536922a6816e5c190b8b0927dca34539fe92e332f730\"}},{\"addressHash\":\"de03d69baeb08c8c6b9a3f65a8c42fe47616ac3762d0a68a6ae2484fe2b3f57d464170914d6daee3fe0de446be402bb58fd2140e6bc3b3e2c1d2ed274b7b4d575e43c13f\",\"amount\":\"23.843926517992237\",\"hash\":\"e891acc744073c8826f2e5bd6a402a5dceeb3b0d4c0257185bb2bc9e9860c02f\",\"createTime\":1535374641328}],\"createTime\":1535374641326,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04bf8bf270293886641e8ff9db722e2dfcb255f3d9464a0fc135e28ad01db9dca0ec5f4b70bb0b980774eaa410350b355da932c0827c334242eb0924c025a954b4\",\"transactionHash\":\"e316642496988b924c3dbe9794f822a507ea2e0883e96808a1c02725c41c1c8d\",\"trustScore\":60.14619,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"94a2292c6770e9641186fdf181225dfceed02728d9a88085a0d2d02c23f538bb\",\"s\":\"c36cf333e58d9a5abeaf4cb72683045cccc29decfc7036c4a2c2274f9e1d75a2\"}}],\"senderHash\":\"04bf8bf270293886641e8ff9db722e2dfcb255f3d9464a0fc135e28ad01db9dca0ec5f4b70bb0b980774eaa410350b355da932c0827c334242eb0924c025a954b4\",\"hash\":\"e316642496988b924c3dbe9794f822a507ea2e0883e96808a1c02725c41c1c8d\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"6529eafbd05a2f31c048742a8e0d63f66f240e664d891bbec37b5fc0db3eaf3d64859f5efa59c6ab6e04c065a11b03c2173848c5831a75cacf307456c1f02ce7f43a8f66\",\"amount\":\"-6.104756795327569\",\"hash\":\"effab3d73b562a7218fbd551d10dd7af1d29ace484f7f77272687db495929122\",\"createTime\":1535374643324,\"signatureData\":{\"r\":\"ae2c002bdd8438fa882e0a153f593c5af7f9bc097f876544fd72870dc7d60e73\",\"s\":\"aa968f3c05ac5bd97e0d22d910b1db173b1e453086b51109686472bd05a5c96e\"}},{\"addressHash\":\"6327c4bf9ad5be5dbff1940e92205dffa4f24ba6940a07efc3ac26074eb5f6677175db2465c1725157f6f93b280293ef7fe987d3fcdc855bfba099bea2e3c51e181e1600\",\"amount\":\"-6.719464806409511\",\"hash\":\"437c9658193249867807f0328b27ea226e25f76c0315a6224ab8d3b677687240\",\"createTime\":1535374643324,\"signatureData\":{\"r\":\"c0d17463d5a530f6d1b0418269b6183f07b8cc41808416531386a2703069ce1c\",\"s\":\"371580ee051d393a76390cfa8995eb77b52d8e59f8c2782e1af98a9493476ced\"}},{\"addressHash\":\"52996c6d45637a9b0525feb7d67ea29e9471a0b4f1bd7e39e42d09e22ca5e651f4ac6bf9d60cedcb03ef941caa66bcdfd5579d0ef4937c4feab076fe025c331d434b5221\",\"amount\":\"-3.7131439277099054\",\"hash\":\"866a1bb464300a7355f456302b7303b2f617037a63279e4ae4b449c36d9682d5\",\"createTime\":1535374643325,\"signatureData\":{\"r\":\"a9e3a7a4d721defb346f3cdcc3beda53f3c29a722a0081cf406412be46f744a9\",\"s\":\"74a7a6316b595e97824adbc2e3665ceba3cdb27534ef21c6334f744e9e1e069d\"}},{\"addressHash\":\"4099989aa51c3d044e866d4508543e67e496d98c0fa317ba25d6c42a1e67f1e84e603034a9d03742f2144c1b752b16d537f615e881660e19db89d9bec03de438167bda93\",\"amount\":\"16.5373655294469854\",\"hash\":\"d411c3153d676e20a0e919cfe2a149b5d8476cb77a587df03c1938a3c8d5a63a\",\"createTime\":1535374643325}],\"createTime\":1535374643322,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"040e8728854eca8ac544f5c8b9dad1755c01d66bc380ddf24e9c956c2b4be3abb114e2b0222a8941ffc42f1384025a85a435998f1602b38067fb51ddb45c144b47\",\"transactionHash\":\"1379cbbe88fd0d0b0134c053d5a15062643c9c1a78fcbeaf478adb7363d94d7f\",\"trustScore\":73.16558,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"46b405c3a1d5474e837ecb737e07d4c3e14d777d9976e99ec1f263ca9f2a85f0\",\"s\":\"bdb645ec12f39bfcbe79d68d7382860470b80050c8e891e50dc00a01fc13f397\"}}],\"senderHash\":\"040e8728854eca8ac544f5c8b9dad1755c01d66bc380ddf24e9c956c2b4be3abb114e2b0222a8941ffc42f1384025a85a435998f1602b38067fb51ddb45c144b47\",\"hash\":\"1379cbbe88fd0d0b0134c053d5a15062643c9c1a78fcbeaf478adb7363d94d7f\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"673fc847b0507953ae104fe26b1ac19c7aa5f6de2d2df02fac88c37780becba1afdbd9007b6846103787eba7720d0424bd2d45f00ce4aeb4c29147fa871055eb0775a129\",\"amount\":\"-8.82840707800771\",\"hash\":\"c48f39c8ad39923552a3c224493e91cd628ddedffa6f4fee985904e0137b2e0a\",\"createTime\":1535374645522,\"signatureData\":{\"r\":\"2f82ffb72dd52a04af968005a79700a0f65afddc64fd97be6d06ebaca773ef6a\",\"s\":\"86b0c82f827c307c787cbec4ac45608a512be3ed144b844abfdc27453bf94d46\"}},{\"addressHash\":\"2646e16c01dde00a16b8a6e1d53d67c2dc970fd2f4c233a8b9a03579a7b5f8bc62e79b71cf7f63007d18653bd97f379f8ad954088458e21a3d4ca0c4378c6f098cba4b8c\",\"amount\":\"-9.406512522669434\",\"hash\":\"0b6e14958c6f7e11d814098943e04a155dac125c6b3bc1da640cb90fb5113688\",\"createTime\":1535374645523,\"signatureData\":{\"r\":\"3ab09527ef97dbbd0c328627cc2c370efa965d66d78d524374a0f0d368b0adb4\",\"s\":\"739f81f9c5364118736297295e9273b813ee8c0d6090c7ccbab9ccc5dd09ae9c\"}},{\"addressHash\":\"bf9b5f7c56e627cfddd16291e6fd8e461f21d827286240391dc85f6ddbdb1d05904ab0f3f3bf4d8c61fe743d8ea878b3fe4c8c0bd1d1cf9d110dcb4b0aaa5f3e9c623505\",\"amount\":\"-6.783894163332031\",\"hash\":\"226e99cb63411054e876e7f6659e79c7fb5518abce1f751b6b41c373ed0baff1\",\"createTime\":1535374645523,\"signatureData\":{\"r\":\"92f4f6ca597f528ed263f27c69b764d44d22776e107a5529310ca3c8fdccbe15\",\"s\":\"a173bb4f175a1e59326f57d0a84743ddd6825322f57693d488c2796d0dd9e749\"}},{\"addressHash\":\"8fe45f976aab174ed37f8e6e84e809f143b5aad43d19aa829b770397df397fbad6a57f47ba43f5855dc8000e615d3d113f6bdd58527d580c80af928072801c23a3cd1289\",\"amount\":\"25.018813764009175\",\"hash\":\"0ff4b4fdd40b3c71979c0a627f6c872803df80312ed9715f6157c448e9cab673\",\"createTime\":1535374645523}],\"createTime\":1535374645521,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04b2442f4e7186e77b7cb85b922d474b108c6938dadbfa3883ab1f893d08cdc1a26057d7370e499b6884bbdfe18ae1e1ceba4239adb1fef31f109b79f406976a96\",\"transactionHash\":\"dfc0066d8a0ebb1420492e92d5db2414fea7e93587da9edbc734dac438c15dfc\",\"trustScore\":76.59502,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"5be1ca1fec8782920051c4ef2c04fb42b3d364966a9adc828417109995bbd85\",\"s\":\"9e2ecb5d8300622b8707db5557dd058cb7d1155ad878f2c6f1fb43873075b7dc\"}}],\"senderHash\":\"04b2442f4e7186e77b7cb85b922d474b108c6938dadbfa3883ab1f893d08cdc1a26057d7370e499b6884bbdfe18ae1e1ceba4239adb1fef31f109b79f406976a96\",\"hash\":\"dfc0066d8a0ebb1420492e92d5db2414fea7e93587da9edbc734dac438c15dfc\"}",
                        "{\"baseTransactions\":[{\"addressHash\":\"1bdff3f25ea1788bcd18d10e424c08040af6f9bc865ff460fb53c4f1d29ca8d62a74b81711e4547a189990d5c7531f26495f6a77c6b8edb39f0f5bde844bf99b4d7c0cb7\",\"amount\":\"-4.854339328678756\",\"hash\":\"1193acfb92b00a4ed80dcb0de48e750039be23a129b2d3dc1e8dd1b96da3ac6c\",\"createTime\":1535374640406,\"signatureData\":{\"r\":\"724884ede8dad25834279bc5c168204a5746f36860c86f7b69255578416c9899\",\"s\":\"39163f5833246b2a15e2c33b11388a08f8af7c30a844610f7353c30e53fef99e\"}},{\"addressHash\":\"ed6aab50af45a6a1df56367500d778abe5a84259b3250aa038705e38902599aa13a0deae1eaf222d04dc7d5a13ab6cd212408a87cc26676fc0e91bbb32cb0300e2069117\",\"amount\":\"-5.0312160590515225\",\"hash\":\"3555f30d21185c026990cc09f25ab1ca7e149bec9bf06370f305021d3f189dbb\",\"createTime\":1535374640407,\"signatureData\":{\"r\":\"2ab159e1f5429428edfb750a4be50d403ea386a08b8929b4f5f8fdfa8f9b26a3\",\"s\":\"3fb7c408869a10d43aba52f1ae67bef7039d54c309597aa78a01d2c38436cc39\"}},{\"addressHash\":\"1c196986603e5764f066fb902667bf18295c0c8422e609c695869878ec07408b982e8ae2a3be576072fad3d53a7c68574a9e84c0bc2727b650870094a255ca9e66f09954\",\"amount\":\"-9.965507548247126\",\"hash\":\"ac613d5e1309a7858187c59ce1c5befad40ba7d64633ded9b0ea477057f84f15\",\"createTime\":1535374640408,\"signatureData\":{\"r\":\"2b1f13f12d22b82f04a1398c73c885681d76b14e105a81d56e0144858ac9587\",\"s\":\"fbab94a12ad045587d18756f2587dd5e54f3b068c880cfb61be2802f06415578\"}},{\"addressHash\":\"ab44d40f5d8976058d2a630afc4b6b5e77c0f4abee8b8275789988e1fd99413aabfd3d1ee9b0a93e3ca65a308c84480e4ccdb75f2c5ec1dcaf1b02e356605b006fdfd720\",\"amount\":\"19.8510629359774045\",\"hash\":\"d033be6f0402d29588a7477458b0f4dcb4a160278ca1abd7e2509e1f1a05b216\",\"createTime\":1535374640408}],\"createTime\":1535374640406,\"transactionDescription\":\"test\",\"trustScoreResults\":[{\"userHash\":\"04cf4e75d7b1c99f61cf7138e3ccd68ff38508165b27eb7458822f1b191042bb635d5c9b64a09c897f08f8ff766f8483968b7370594e876baa0f7b895e2d2c36c2\",\"transactionHash\":\"8727b3ecf6bb3f384b9ba992f77992dea0fc913ba2a3f199739d1c09551ad2b5\",\"trustScore\":30.23349,\"trustScoreNodeHash\":\"bb8402f6b0f01d31ffb44799ae33d868572bbd349e17e2c9187e08abe64d1e65cd4b35a26f9f229629f2e6082abeadbb690d1147db1d52857dacb88dc313f389\",\"trustScoreNodeSignature\":{\"r\":\"575847094292e1dcd6e5fa8252555cd096398ecb0960515a0566122dc59f0d2e\",\"s\":\"a7f297c2aa556a11181fddd13a7d6f664632660df86bfc822784caa79abfb0a\"}}],\"senderHash\":\"04cf4e75d7b1c99f61cf7138e3ccd68ff38508165b27eb7458822f1b191042bb635d5c9b64a09c897f08f8ff766f8483968b7370594e876baa0f7b895e2d2c36c2\",\"hash\":\"8727b3ecf6bb3f384b9ba992f77992dea0fc913ba2a3f199739d1c09551ad2b5\"}"
                ));
    }
//    private final static String transactionDescription = "message";
//    private final static SignatureData signatureMessage = new SignatureData("message", "message");
//    @Autowired
//    private TransactionController transactionController;
//    @Autowired
//    private IBalanceService balanceService;
//    @Autowired
//    private TransactionService transactionService;
//
//    private int privatekeyInt = 122;
    /*
       This is a good scenario where amount and address are dynamically generated
      */


    @Test
    public void aTestFullProcess() {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clusterService.checkForTrustChainConfirmedTransaction();
        }
        ).start();

        transactionsRequestAsJsons.forEach(transactionJsonRequest -> {
            AddTransactionRequest transactionRequest =
                    TestUtils.createTransactionRequestFromJson(transactionJsonRequest);
            transactionController.addTransaction(transactionRequest);
        });


    }
//    @Test
//    public void bTestBadScenarioNewTransactionNegativeAmount() {
//        String baseTransactionHexaAddress = TestUtils.getRandomHexa();
//        Hash fromAddress = new Hash(TestUtils.getRandomHexa());
//
//        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress, 3);
////        replaceBalancesWithAmount(fromAddress, new BigDecimal(2));
//        ResponseEntity<Response> badResponseEntity = transactionService.
//                addNewTransaction(createRequestWithOneBaseTransaction(new Hash("AB"), fromAddress,
//                        new Hash(baseTransactionHexaAddress), new BigDecimal(3)));
//        Assert.assertTrue(badResponseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
//        Assert.assertTrue(badResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_ERROR));
//
//    }

//
//    @Test
//    public void cTestBadScenarioNotEnoughSourcesForTcc() {
//        Hash fromAddress = new Hash(TestUtils.getRandomHexa());
////        updateBalancesWithAddressAndAmount(fromAddress, new BigDecimal(100));
//        String baseTransactionHexaAddress = TestUtils.getRandomHexa();
//        BigDecimal plusAmount = new BigDecimal(50);
//        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress, plusAmount);
//        ResponseEntity<Response> goodResponseEntity = transactionService.
//                addNoneIndexedTransaction(createRequestWithOneBaseTransaction(new Hash("AC"), fromAddress
//                        , new Hash(baseTransactionHexaAddress), plusAmount));
//        Assert.assertTrue(goodResponseEntity.getStatusCode().equals(HttpStatus.CREATED));
//        Assert.assertTrue(goodResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS));
//
//        String baseTransactionHexaAddress2 = TestUtils.getRandomHexa();
////        replaceBalancesWithAmount(fromAddress, new BigDecimal(50));
//        log.info("Base transaction {} with amount {} about to be sent ", baseTransactionHexaAddress2, 60);
//        ResponseEntity<Response> badResponseEntity = transactionService.
//                addNoneIndexedTransaction(createRequestWithOneBaseTransaction(new Hash("AD"),
//                        fromAddress, new Hash(baseTransactionHexaAddress), new BigDecimal(60)));
//        Assert.assertTrue(badResponseEntity.getStatusCode().equals(HttpStatus.UNAUTHORIZED));
//        Assert.assertTrue(badResponseEntity.getBody().getStatus().equals(HttpStringConstants.STATUS_ERROR));
//
//        ConfirmationData confirmationData = confirmedTransactions.getByHash(new Hash("AD"));
//        Assert.assertNull(confirmationData);
//
//        try {
//            log.info("CotiNodeTest is going to sleep for 20 sec");
//            TimeUnit.SECONDS.sleep(20);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        confirmationData = confirmedTransactions.getByHash(new Hash("AC"));
//        Assert.assertNull(confirmationData); // The transaction doesn't have enough sources before it
//
//    }

//    @Test
//    public void dTestSimpleRollBack() {
//        Map<Hash, BigDecimal> preBalanceMap = balanceService.getPreBalanceMap();
//        Hash address1 = new Hash("ABCD");
//        Hash address2 = new Hash("ABCDEF");
//
//        BaseTransactionData btd1 = new BaseTransactionData(address1, new BigDecimal(5.5), address1, new SignatureData("", ""), new Date());
//        BaseTransactionData btd2 = new BaseTransactionData(address2, new BigDecimal(6.57), address2, new SignatureData("", ""), new Date());
//        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
//        baseTransactionDataList.add(btd1);
//        baseTransactionDataList.add(btd2);
//        // balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);
//        updateBalancesWithAddressAndAmount(address1, new BigDecimal(5.5));
//        updateBalancesWithAddressAndAmount(address2, new BigDecimal(6.57));
//
////        balanceService.rollbackBaseTransactions(baseTransactionDataList);
//
//        Assert.assertTrue(preBalanceMap.get(address1).compareTo(BigDecimal.ZERO) == 0);
//
//        Assert.assertTrue(preBalanceMap.get(address2).compareTo(BigDecimal.ZERO) == 0);
//
//    }
//
//    private void updateBalancesWithAddressAndAmount(Hash hash, BigDecimal amount) {
//        Map<Hash, BigDecimal> balanceMap = balanceService.getBalanceMap();
//        Map<Hash, BigDecimal> preBalanceMap = balanceService.getPreBalanceMap();
//        if (balanceMap.containsKey(hash)) {
//            balanceMap.put(hash, amount.add(balanceMap.get(hash)));
//        } else {
//            balanceMap.put(hash, amount);
//        }
//        if (preBalanceMap.containsKey(hash)) {
//            preBalanceMap.put(hash, amount.add(preBalanceMap.get(hash)));
//        } else {
//            preBalanceMap.put(hash, amount);
//        }
//    }
//
//    private void replaceBalancesWithAmount(Hash hash, BigDecimal amount) {
//        Map<Hash, BigDecimal> balanceMap = balanceService.getBalanceMap();
//        Map<Hash, BigDecimal> preBalanceMap = balanceService.getPreBalanceMap();
//
//        balanceMap.put(hash, amount);
//        preBalanceMap.put(hash, amount);
//
//
//    }
/*
    private AddTransactionRequest createRequestWithOneBaseTransaction(Hash transactionHash, Hash fromAddress, Hash baseTransactionAddress, BigDecimal amount) {
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();

//        BaseTransactionData baseTransactionData =
//                new BaseTransactionData(new Hash(CryptoUtils.getPublicKeyFromPrivateKey(BigInteger.valueOf(123)).toByteArray()),
//                        amount, baseTransactionAddress,
//                        signatureMessage, new Date());


        BaseTransactionData myBaseTransactionData =
                new BaseTransactionData(fromAddress, amount.negate()
                        , new Hash("AB"),
                        signatureMessage, new Date());


//        baseTransactionDataList.add(baseTransactionData);
        baseTransactionDataList.add(myBaseTransactionData);


        AddTransactionRequest addTransactionRequest = new AddTransactionRequest();
        addTransactionRequest.baseTransactions = baseTransactionDataList;
        addTransactionRequest.hash = transactionHash;
        addTransactionRequest.transactionDescription = transactionDescription;
        return addTransactionRequest;
    }


    private List<BaseTransactionData> createBaseTransactionRandomList(int numOfBaseTransactions) {
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        Hash myAddress = new Hash(TestUtils.getRandomHexa());
//        updateBalancesWithAddressAndAmount(myAddress, new BigDecimal(100 * numOfBaseTransactions));

        for (int i = 0; i < numOfBaseTransactions; i++) {
            privatekeyInt++;
            BigDecimal amount = new BigDecimal(TestUtils.getRandomDouble()).setScale(2, BigDecimal.ROUND_CEILING);
            BigInteger privateKey = BigInteger.valueOf(privatekeyInt);
//            BaseTransactionData baseTransactionData =
//                    new BaseTransactionData(new Hash(CryptoUtils.getPublicKeyFromPrivateKey(privateKey).toByteArray()), amount
//                            , new Hash(TestUtils.getRandomHexa()),
//                            signatureMessage, new Date());

            BaseTransactionData myBaseTransactionData =
                    new BaseTransactionData(myAddress, amount.negate()
                            , myAddress,
                            signatureMessage, new Date());


//            baseTransactionDataList.add(baseTransactionData);
            baseTransactionDataList.add(myBaseTransactionData);

        }
        return baseTransactionDataList;
    }
    */

}
