package unitTest;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetTransactionTrustScoreResponse;
import io.coti.basenode.http.GetUserTrustScoreResponse;
import io.coti.basenode.http.SetKycTrustScoreRequest;
import io.coti.trustscore.AppConfig;
import io.coti.trustscore.services.TrustScoreService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@SpringBootTest
public class TrustScoreServiceTest {
    @Autowired
    private TrustScoreService trustScoreService;

    @Before
    public void init() {
        SetKycTrustScoreRequest setKycTrustScoreRequest = new SetKycTrustScoreRequest();
        setKycTrustScoreRequest.userHash = new Hash("042d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702");
        setKycTrustScoreRequest.signature =
                new SignatureData("7d5de35d37d22fadb7e86a50c081dcfa65cd130ae6a3eab7539c0596990c1292",
                        "bcbaad1569b66674161602cbb832bfc3aee24efd2d20063e0d24eb6eb852468f");
        setKycTrustScoreRequest.kycTrustScore = 22.43456;
        trustScoreService.setKycTrustScore(setKycTrustScoreRequest);
    }

    @Test
    public void getTransactionTrustScore() {
        ResponseEntity<BaseResponse> response =
                trustScoreService.getTransactionTrustScore(new Hash("042d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702"),
                        new Hash("5e7a93aeaac5c41ff97646d53df28329bcbed9952dad02a9deccf85ccdf70681"));
        double trustScore = ((GetTransactionTrustScoreResponse) response.getBody())
                .getTransactionTrustScoreData().getTrustScore();
        Assert.assertTrue(trustScore == 22.43456);
    }

    @Test
    public void getUserTrustScore() {

        ResponseEntity<BaseResponse> response =
                trustScoreService.getUserTrustScore(new Hash("042d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702"));
        double trustScore = ((GetUserTrustScoreResponse) response.getBody()).getTrustScore();
        Assert.assertTrue(trustScore == 22.43456);
    }

}
