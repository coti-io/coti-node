package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.data.TrustScoreData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUserTrustScoreComponentsResponse extends BaseResponse {

    private TrustScoreData userTrustScoreData;
    private List<BucketEventData<? extends EventData>> bucketEventDataList;

    public GetUserTrustScoreComponentsResponse(TrustScoreData userTrustScoreResponseData, List<BucketEventData<? extends EventData>> bucketEventDataListResponseData) {
        this.userTrustScoreData = userTrustScoreResponseData;
        this.bucketEventDataList = bucketEventDataListResponseData;
    }

}
