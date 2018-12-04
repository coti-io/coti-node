package io.coti.trustscore.services;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.config.rules.UserNetworkFeeByTrustScoreRange;
import io.coti.trustscore.data.Enums.TrustScoreRangeType;
import io.coti.trustscore.http.GetNetworkFeeRequest;
import io.coti.trustscore.http.GetNetworkFeeResponse;
import io.coti.trustscore.http.GetUserTrustScoreResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@Data
public class NetworkFeeService {

    @Autowired
    private TrustScoreService trustScoreService;

    public ResponseEntity<BaseResponse> getNetworkFeeAmount(GetNetworkFeeRequest request) {
        ResponseEntity userTrustScoreResponse =
                trustScoreService.getUserTrustScore(request.userHash);
        if (userTrustScoreResponse.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            return userTrustScoreResponse;
        }
        double trustScore = ((GetUserTrustScoreResponse) trustScoreService.getUserTrustScore(request.userHash).getBody()).getTrustScore();
        UserNetworkFeeByTrustScoreRange userNetworkFeeByTrustScoreRange = getUserNetworkFeeByTrustScoreRange(trustScore);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetNetworkFeeResponse(request.userHash.toHexString(), calculateNetworkFeeAmount(userNetworkFeeByTrustScoreRange, request.amount)));
    }

    private UserNetworkFeeByTrustScoreRange getUserNetworkFeeByTrustScoreRange(double trustScore) {
        Map<TrustScoreRangeType, UserNetworkFeeByTrustScoreRange> trustScoreRangeTypeToUserScoreMap
                = trustScoreService.getRulesData().getTrustScoreRangeTypeToUserScoreMap();
        if (trustScore < trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.LOW).getLimit()) {
            return trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.LOW);
        } else if (trustScore > trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.HIGH).getLimit()) {
            return trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.HIGH);
        }
        return trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.STANDARD);
    }

    private double calculateNetworkFeeAmount(UserNetworkFeeByTrustScoreRange userNetworkFeeByTrustScoreRange, double amount) {
        // formula calculation:
        return Math.min(Math.max(userNetworkFeeByTrustScoreRange.getFeeRate() * amount, userNetworkFeeByTrustScoreRange.getMinRate())
                , userNetworkFeeByTrustScoreRange.getMaxRate());
    }
}
