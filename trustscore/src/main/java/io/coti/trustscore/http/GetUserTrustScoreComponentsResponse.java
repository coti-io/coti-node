package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.UserTrustScoreData;
import io.coti.trustscore.data.tsbuckets.BucketData;
import io.coti.trustscore.http.data.BucketResponseData;
import io.coti.trustscore.http.data.UserTrustScoreResponseData;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class GetUserTrustScoreComponentsResponse extends BaseResponse {
    private UserTrustScoreResponseData userTrustScoreData;
    private List<BucketResponseData> bucketEventDataList = new LinkedList<>();

    public GetUserTrustScoreComponentsResponse(UserTrustScoreData userTrustScoreData, List<BucketData> bucketEventDataListResponseData) {
        this.userTrustScoreData = new UserTrustScoreResponseData(userTrustScoreData);

        for (BucketData bucketData : bucketEventDataListResponseData) {
            bucketEventDataList.add(new BucketResponseData(bucketData));
        }
    }

}