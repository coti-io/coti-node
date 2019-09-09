package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.UserTrustScoreData;
import io.coti.trustscore.data.scorebuckets.BucketData;
import lombok.Data;

import java.util.List;

@Data
public class GetUserTrustScoreComponentsResponse extends BaseResponse {
    private static final long serialVersionUID = 3005418696077773256L;
    private UserTrustScoreData userTrustScoreData;
    private List<BucketData> bucketEventDataList;

    public GetUserTrustScoreComponentsResponse(UserTrustScoreData userTrustScoreData, List<BucketData> bucketEventDataListResponseData) {
        this.userTrustScoreData = userTrustScoreData;
        this.bucketEventDataList = bucketEventDataListResponseData;
    }

}