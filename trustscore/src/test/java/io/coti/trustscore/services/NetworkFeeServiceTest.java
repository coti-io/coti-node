package io.coti.trustscore.services;

import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.crypto.TrustScoreCrypto;
import io.coti.trustscore.crypto.TrustScoreEventCrypto;
import io.coti.trustscore.crypto.TrustScoreUserTypeCrypto;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.database.TrustScoreRocksDBConnector;
import io.coti.trustscore.http.GetNetworkFeeRequest;
import io.coti.trustscore.http.GetNetworkFeeResponse;
import io.coti.trustscore.http.SetKycTrustScoreRequest;
import io.coti.trustscore.model.BucketEvents;
import io.coti.trustscore.model.TrustScores;
import io.coti.trustscore.model.UserTypeOfUsers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketTransactionService.class,
        TrustScoreService.class,
        RocksDBConnector.class,
        BucketBehaviorEventsService.class,
        BucketInitialTrustScoreEventsService.class,
        BucketNotFulfilmentEventsService.class,
        BucketChargeBackEventsService.class,
        TrustScoreRocksDBConnector.class,
        DBInitializerService.class,
        BucketEvents.class,
        UserTypeOfUsers.class,
        TrustScores.class,
        NetworkFeeService.class,
        TrustScoreUserTypeCrypto.class
})

public class NetworkFeeServiceTest {

    @Autowired
    private NetworkFeeService networkFeeService;

    @Autowired
    private TrustScoreService trustScoreService;

    @Autowired
    private DBInitializerService DBInitializerService;

    @Autowired
    private TrustScoreRocksDBConnector trustScoreRocksDBConnector;

    @Autowired
    private TrustScores trustScores;

    @MockBean
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;

    @MockBean
    private TrustScoreCrypto trustScoreCrypto;

    @MockBean
    private TrustScoreEventCrypto trustScoreEventCrypto;

    @MockBean
    private UserTypeOfUsers userTypeOfUsers;

    @Before
    public void setUp() {
        when(
                trustScoreCrypto.verifySignature(
                        any(TrustScoreData.class)
                )
        ).thenReturn(true);
        SetKycTrustScoreRequest setKycTrustScoreRequest = new SetKycTrustScoreRequest();
        setKycTrustScoreRequest.userHash = new Hash("2d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702");
        setKycTrustScoreRequest.signature = new SignatureData("r1", "s1");
        setKycTrustScoreRequest.kycTrustScore = 4;
        setKycTrustScoreRequest.userType = UserType.WALLET.toString();
        trustScoreService.setKycTrustScore(setKycTrustScoreRequest);

    }
/*
    @Test
    public void getNetworkFeeAmount() {

        GetNetworkFeeRequest getNetworkFeeRequest = new GetNetworkFeeRequest();
        getNetworkFeeRequest.userHash = new Hash("2d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702");
        getNetworkFeeRequest.amount = 12;
        double rollingReserveNeededAmount =
                ((GetNetworkFeeResponse) networkFeeService.getNetworkFeeAmount(getNetworkFeeRequest).getBody()).getNetworkFeeAmount();
        Assert.assertTrue(rollingReserveNeededAmount == 6);
    }*/
}