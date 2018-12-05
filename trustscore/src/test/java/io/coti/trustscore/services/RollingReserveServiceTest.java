package io.coti.trustscore.services;

import io.coti.basenode.database.RocksDBConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {
        //RollingReserveService.class,
        TrustScoreService.class,
        RocksDBConnector.class
})
public class RollingReserveServiceTest {

//    @Autowired
//    private RollingReserveService rollingReserveService;

    @MockBean
    private TrustScoreService trustScoreService;

    @Test
    public void getRollingReserveNeededAmount() {
//        when(
//                trustScoreService.getUserTrustScore(
//                        any(Hash.class)
//                )
//        ).thenReturn(ResponseEntity.status(HttpStatus.OK)
//                .body(new GetUserTrustScoreResponse("2d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702",
//                        4)));
//        GetRollingReserveRequest getRollingReserveRequest = new GetRollingReserveRequest();
//        getRollingReserveRequest.userHash = new Hash("2d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702");
//        getRollingReserveRequest.amount = 12;
//        double rollingReserveNeededAmount =
//                ((GetRollingReserveResponse) rollingReserveService.getRollingReserveNeededAmount(getRollingReserveRequest).getBody()).getRollingReserveAmount();
//        Assert.assertTrue(rollingReserveNeededAmount == 300);
    }
}