package io.coti.trustscore.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.data.interfaces.IResponseData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.TrustScoreData;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class TrustScoreResponseData implements IResponseData {

    private String userHash;
    private Double kycTrustScore;
    private SignatureData signature;
    private String kycServerPublicKey;
    private Date createTime;
    private Map<EventType, String> eventTypeToBucketHashMap;
    private UserType userType;
    private Boolean zeroTrustFlag;

    public TrustScoreResponseData(TrustScoreData trustScoreData) {
        userHash = trustScoreData.getUserHash().toString();
        kycTrustScore = trustScoreData.getKycTrustScore();
        signature = trustScoreData.getSignature();
        createTime = trustScoreData.getCreateTime();
        eventTypeToBucketHashMap = trustScoreData.getEventTypeToBucketHashMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toString()));
        userType = trustScoreData.getUserType();
        zeroTrustFlag = trustScoreData.getZeroTrustFlag();
    }
}
