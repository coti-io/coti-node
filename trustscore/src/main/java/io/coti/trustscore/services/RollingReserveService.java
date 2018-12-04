package io.coti.trustscore.services;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.GetRollingReserveRequest;
import io.coti.trustscore.http.GetRollingReserveResponse;
import io.coti.trustscore.http.GetUserTrustScoreResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Data
public class RollingReserveService {
    private static final double MAX_ROLLING_RESERVE_RATE = 100;
    @Autowired
    private TrustScoreService trustScoreService;

    public ResponseEntity<BaseResponse> getRollingReserveNeededAmount(GetRollingReserveRequest request) {
        ResponseEntity userTrustScoreResponse =
                trustScoreService.getUserTrustScore(request.userHash);
        if (userTrustScoreResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            return userTrustScoreResponse;
        }
        double trustScore = ((GetUserTrustScoreResponse) trustScoreService.getUserTrustScore(request.userHash).getBody()).getTrustScore();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetRollingReserveResponse(request.userHash.toHexString(), calculateRollingReserveNeededRate(trustScore) * request.amount));
    }

    private double calculateRollingReserveNeededRate(double trustScore) {
        // Rolling Reserve calculation formula
        return (trustScore == 0) ? MAX_ROLLING_RESERVE_RATE : Math.min(MAX_ROLLING_RESERVE_RATE / trustScore, MAX_ROLLING_RESERVE_RATE);
    }
}
