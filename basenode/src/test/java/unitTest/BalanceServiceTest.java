package unitTest;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

//import io.coti.basenode.services.LiveView.BaseNodeWebSocketSender;

@TestPropertySource(locations = "../test.properties")
@ContextConfiguration(classes = BaseNodeBalanceService.class)
@SpringBootTest
@RunWith(SpringRunner.class)

public class BalanceServiceTest {

    @Autowired
    private BaseNodeBalanceService balanceService;
    @MockBean
    private LiveViewService liveViewService;
    @MockBean
    private Transactions transactions;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    TransactionHelper transactionHelper;
    @MockBean
    BaseNodeInitializationService initializationService;

    @Before
    public void init() throws Exception {
        balanceService.init();
    }

    @Test
    public void balanceAmount_AsExpected() {
        BigDecimal addressBalance = balanceService.getBalanceMap().
                get(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"));
        Assert.assertEquals(addressBalance, new BigDecimal("3941622.610838615"));
    }

    @Test
    public void balanceAmount_AsNotExpected() {
        BigDecimal addressBalance = balanceService.getBalanceMap().
                get(new Hash("5e6b6af708ae15c1c55641f9e87e71f5cd58fc71aa58ae55abe9d5aa88b2ad3c5295cbffcfbb3a087e8da72596d7c60eebb4c59748cc1906b2aa67be43ec3eb147c1a19a"));
        Assert.assertNotEquals(addressBalance, new BigDecimal("3941622.610838615"));
    }

    @Test
    public void passingBalanceAndPreBalanceCheck_WhenEnoughCotiInAddress() {
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.
                add(new BaseTransactionData(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                        new BigDecimal(-3000000),
                        new Hash("AE"),
                        new SignatureData("", ""),
                        new Date()));

        boolean ans = balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);
        Assert.assertTrue(ans);
    }

    @Test
    public void passingBalanceAndPreBalanceCheck_WhenNotEnoughCotiInAddress() {
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.
                add(new BaseTransactionData(new Hash("8b4a58b51c89d52f41f67496389e79104ff3bf98e59b5766cd8d3683b8fbf653b51f403ee2d82aabc7215b6b046787c507a69749a070290b9a44c3a10389ab125545ea76"),
                        new BigDecimal(-2000000),
                        new Hash("BE"),
                        new SignatureData("", ""),
                        new Date()));

        boolean ans = balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);
        Assert.assertFalse(ans);
    }

    @Test
    public void passingBalanceAndPreBalanceCheck_WhenAddressNotExist() {
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.
                add(new BaseTransactionData(new Hash("8b4a58b51c89d52f41f67496389e79104ff3bf98e59b5766cd8d3683b8fbf653b51f403ee2d82aabc7215b6b046787c507a69749a070290b9a44c3a10389ab125548ab12"),
                        new BigDecimal(-200),
                        new Hash("CE"),
                        new SignatureData("", ""),
                        new Date()));

        boolean ans = balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);
        Assert.assertFalse(ans);
    }

    @Test
    public void getBalances_addressHashInBalanceMap_correctBalanceAmountInResponse() {
        Hash hash = new Hash("3b4964b8b7b7cdef275ae49d82913dc7e4bf5ee890d4ef47422b2aec3f11f7900e64930d6cf4eaa4e8a62898043fbbe09d29f54cdab51653912bc926754c80174d7d9401");
        ResponseEntity<GetBalancesResponse> response = getBalance(Arrays.asList(hash));
        Assert.assertEquals(response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressBalance(), new BigDecimal("23423423.678"));
    }

    @Test
    public void getBalances_addressHashNotInBalanceMap_balanceZeroInResponse() {
        Hash hash = new Hash("3b4964b8b7b7cdef275ae49d82913dc7e4bf5ee890d4ef47422b2aec3f11f7900e64930d6cf4eaa4e8a62898043fbbe09d29f54cdab51653912bc926754c80174d7d1234");
        ResponseEntity<GetBalancesResponse> response = getBalance(Arrays.asList(hash));
        Assert.assertEquals(response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressBalance(), new BigDecimal("0"));
    }

    @Test
    public void getBalances_addingAmountToExistAddress_correctNewPreBalanceAmountInResponse() {
        Hash hash = new Hash("f4581d2c6b53e1d6cf018040d83f1b01bbe91983a0bf0a0ec6823a8bc4ccd99c66e3a87e3fe1862aa0f5c907e56ecfa99ecf0e04883377c293c8f49a647a52c657cdb46a");
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.
                add(new BaseTransactionData(hash,
                        new BigDecimal(-5000),
                        new Hash("DE"),
                        new SignatureData("", ""),
                        new Date()));
        balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);
        ResponseEntity<GetBalancesResponse> response = getBalance(Arrays.asList(hash));
        Assert.assertEquals(response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressPreBalance(), new BigDecimal("3932240.71924616"));
    }

    @Test
    public void getBalances_addingAmountToExistAddress_newBalanceAmountInResponseNotAffected() {
        Hash hash = new Hash("ed055b792cd7f322b006cd037f82fb151b7e5f1a14721f2595a6c78cc00266542fccdbc34cf9dcdd558128bead26073412dfd6dd9a389cbf06ec2a93e348ddde3f2678f2");
        List<BaseTransactionData> baseTransactionDataList = new LinkedList<>();
        baseTransactionDataList.
                add(new BaseTransactionData(hash,
                        new BigDecimal(-4000),
                        new Hash("EE"),
                        new SignatureData("", ""),
                        new Date()));
        balanceService.checkBalancesAndAddToPreBalance(baseTransactionDataList);

        ResponseEntity<GetBalancesResponse> response = getBalance(Arrays.asList(hash));
        Assert.assertEquals(response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressBalance(), new BigDecimal("9099122.237011632"));
    }

    @Test
    public void rollbackBaseTransactions() {
        Hash hash = new Hash("f345689c529d0a9dc3bb6d32b12fa70a2886a6984ee7ddf9e6796a9416cd7dc88be6e0c8ad6402e512e1e7254619a1c1a152065ba449616526fa04b0dd5c63517a6abf06");
        TransactionData transactionData = new TransactionData(Arrays.asList(new BaseTransactionData(hash,
                new BigDecimal(-2000),
                new Hash("HE"),
                new SignatureData("", ""),
                new Date())), new Hash("ccccc"), "test", 82, new Date());
        when(transactionHelper.isConfirmed(transactionData)).thenReturn(false);
        balanceService.insertSavedTransaction(transactionData);
        ResponseEntity<GetBalancesResponse> response = getBalance(Arrays.asList(hash));
        BigDecimal preBalanceBeforeRollback = response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressPreBalance();

        balanceService.rollbackBaseTransactions(transactionData);

        response = getBalance(Arrays.asList(hash));
        BigDecimal preBalanceAfterRollback = response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressPreBalance();
        Assert.assertEquals(preBalanceBeforeRollback.add(new BigDecimal(2000)), preBalanceAfterRollback);
    }

    @Test
    public void insertSavedTransaction_whenIsConformed_InsertBalancedAndPreBalance() {
        Hash hash = new Hash("fa05d0ea7484aeb9b1b7a3c34960dadcf38f4bfafad4725671392d1f2fe73b473a22f4abf8bbc125bbf953ca4657dbaaddde7368d9c46fb83f4f6c0aa488bbf45c335a3e");
        TransactionData transactionData = new TransactionData(Arrays.asList(new BaseTransactionData(hash,
                new BigDecimal(-4000),
                new Hash("FE"),
                new SignatureData("", ""),
                new Date())), new Hash("aaaaaa"), "test", 92, new Date());

        when(transactionHelper.isConfirmed(transactionData)).thenReturn(true);
        balanceService.insertSavedTransaction(transactionData);
        ResponseEntity<GetBalancesResponse> response = getBalance(Arrays.asList(hash));
        BigDecimal updatedBalance = response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressBalance();
        BigDecimal updatedPreBalance = response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressPreBalance();
        BigDecimal expectedUpdatedBalanceAndPreBalance = new BigDecimal("8650892.315172933");
        Assert.assertTrue(updatedBalance.compareTo(expectedUpdatedBalanceAndPreBalance) == 0 &&
                updatedPreBalance.compareTo(expectedUpdatedBalanceAndPreBalance) == 0);
    }

    @Test
    public void insertSavedTransaction_whenIsNotConformed_InsertOnlyInPreBalance() {
        Hash hash = new Hash("330e5f410d6d07aec8722e3da94c81504e4eb558d2c462c1d228f12c74c9b24304ad83197e73f299130be557e78ce07e5569d6a4545cd4c5cbd1af219693e8b769f19aca");
        TransactionData transactionData = new TransactionData(Arrays.asList(new BaseTransactionData(hash,
                new BigDecimal(-4000),
                new Hash("GE"),
                new SignatureData("", ""),
                new Date())), new Hash("bbbbbb"), "test", 82, new Date());
        when(transactionHelper.isConfirmed(transactionData)).thenReturn(false);
        balanceService.insertSavedTransaction(transactionData);
        ResponseEntity<GetBalancesResponse> response = getBalance(Arrays.asList(hash));
        BigDecimal updatedBalance = response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressBalance();
        BigDecimal updatedPreBalance = response.getBody().getAddressesBalance().get(hash.toHexString()).getAddressPreBalance();
        Assert.assertTrue(updatedBalance.compareTo(new BigDecimal("5478282.176401698")) == 0 &&
                updatedPreBalance.compareTo(new BigDecimal("5474282.176401698")) == 0);
    }

    @Test
    public void finalizeInit_noExceptionIsThrown() {
        try {
            balanceService.finalizeInit();
        } catch (Exception e) {
            assertNull(e);
        }
    }

    //run this test only individually
//    @Test
//    public void setTccToTrue_noExceptionIsThrown() {
//        try {
//            balanceService.setTccToTrue(new TccInfo(null, null, 0));
//        } catch (Exception e) {
//            assertNull(e);
//        }
//    }

    //run this test only individually
//    @Test
//    public void setDspcToTrue_noExceptionIsThrown() {
//        try {
//            balanceService.setDspcToTrue(new DspConsensusResult(null));
//        } catch (Exception e) {
//            assertNull(e);
//        }
//    }

    private ResponseEntity<GetBalancesResponse> getBalance(List<Hash> addresses) {
        GetBalancesRequest getBalancesRequest = new GetBalancesRequest();
        getBalancesRequest.setAddresses(addresses);
        return balanceService.getBalances(getBalancesRequest);
    }

    public static class CryptoHelperTest {

        @Test
        public void getPublicKeyFromHexString() throws InvalidKeySpecException, NoSuchAlgorithmException {
            PublicKey publicKey
                    = CryptoHelper.getPublicKeyFromHexString("3d070c0014fdeb9e9522f1bcd00c86cb9ebeadc142a0d40c73ddda58bb82fb61f5b0d20dd55bf90a378bacb28036b0ddafe743b7e452a3fd11f05b78f14f0f99");
            String xCoord = ((BCECPublicKey) publicKey).getQ().getXCoord().toString();
            String yCoord = ((BCECPublicKey) publicKey).getQ().getYCoord().toString();
            Assert.assertTrue(xCoord.equals("3d070c0014fdeb9e9522f1bcd00c86cb9ebeadc142a0d40c73ddda58bb82fb61")
                    && yCoord.equals("f5b0d20dd55bf90a378bacb28036b0ddafe743b7e452a3fd11f05b78f14f0f99"));
        }

        @Test
        public void verifyByPublicKey() {
        }

        @Test
        public void removeLeadingZerosFromAddress() {
        }

        @Test
        public void signBytes() {
            // SignatureData SignatureData1 = CryptoHelper.SignBytes("[B@2ddb3ae8".getBytes(Charset.forName("UTF-8")), "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f83");
            SignatureData SignatureData = CryptoHelper.SignBytes("[B@2ddb3ae8".getBytes(), "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f83");
            Assert.assertTrue(SignatureData.getR().length() == 64
                    && SignatureData.getS().length() == 64);
        }

        @Test
        public void getPublicKeyFromPrivateKey() {
            // TODO
        }

        @Test
        public void verifyByPublicKey1() {
            // TODO
        }

        @Test
        public void generateKeyPair() {
            // TODO
        }

        @Test
        public void isAddressValid_validAddress_returnTrue() {
            boolean validatedHash =
                    CryptoHelper.IsAddressValid(new Hash("fd33fe95a50fb2e458449872412e76a57aabbb6378e48a0aad61ce9cd6a7fae44e80f336820939cefad94c4000e7674f8921fa8ac10335f7fc24dea5728234eadee36524"));
            Assert.assertTrue(validatedHash);
        }

        @Test
        public void isAddressValid_notValidAddress_returnFalse() {
            boolean validatedHash =
                    CryptoHelper.IsAddressValid(new Hash("dd33fe95a50fb2e458449872412e76a57aabbb6378e48a0aad61ce9cd6a7fae44e80f336820939cefad94c4000e7674f8921fa8ac10335f7fc24dea5728234eadee36524"));
            Assert.assertFalse(validatedHash);
        }

        @Test
        public void getAddressFromPrivateKey() {
            Hash addressHash = CryptoHelper.getAddressFromPrivateKey("1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f8e");
            Assert.assertTrue(addressHash.toString().equals(
                    "a053a4ddfd9c4e27b919a26ccb2d99a55f679c13fec197efc48fc887661a626db19a99660f8ae3babddebf924923afb22c7d4fe251f96f1880c4b8f89106d139fd5a8f93"));
        }

        @Test
        public void cryptoHash() {
            Hash cryptoHash = CryptoHelper.cryptoHash("GENESIS".getBytes());
            Assert.assertTrue(cryptoHash.toString().equals("019f6193080fa2ce1eb4082321d3fc1563ca3ee6f96dc5b2092d4bd08cc1b2cb"));
        }
    }
}


