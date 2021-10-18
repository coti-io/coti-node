package io.coti.trustscore.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.HighFrequencyEventScoreType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetHighFrequencyEventScoreResponse extends BaseResponse {

    private String userHash;
    private int eventType;
    private String highFrequencyEventScoreType;
    private String transactionDataHash;

    public SetHighFrequencyEventScoreResponse(Hash userHash,
                                              EventType eventType,
                                              HighFrequencyEventScoreType highFrequencyEventScoreType,
                                              Hash transactionDataHash) {
        this.userHash = userHash.toHexString();
        this.eventType = eventType.getValue();
        this.highFrequencyEventScoreType = highFrequencyEventScoreType.toString();
        this.transactionDataHash = (transactionDataHash != null) ? transactionDataHash.toHexString() : null;
    }
}
