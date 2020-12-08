package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.http.data.BucketEventResponseData;
import io.coti.trustscore.http.data.TrustScoreResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUserTrustScoreComponentsResponse extends BaseResponse {

    private TrustScoreResponseData userTrustScoreData;
    private List<BucketEventResponseData<? extends EventData>> bucketEventDataList;

    public GetUserTrustScoreComponentsResponse(TrustScoreData userTrustScoreResponseData, List<BucketEventData<? extends EventData>> bucketEventDataListResponseData) {
        this.userTrustScoreData = new TrustScoreResponseData(userTrustScoreResponseData);
        this.bucketEventDataList = bucketEventDataListResponseData.stream().map(BucketEventResponseData::new).collect(Collectors.toList());
    }

}
