package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.TrustScoreData;
import lombok.Data;

import java.util.List;

@Data
public class GetUserTrustScoreComponentsResponse extends BaseResponse {
    private TrustScoreData userTrustScoreData;
    private List<BucketEventData> bucketEventDataList;

    public GetUserTrustScoreComponentsResponse(TrustScoreData userTrustScoreResponseData, List<BucketEventData> bucketEventDataListResponseData) {
        this.userTrustScoreData = userTrustScoreResponseData;
        this.bucketEventDataList = bucketEventDataListResponseData;
    }

}