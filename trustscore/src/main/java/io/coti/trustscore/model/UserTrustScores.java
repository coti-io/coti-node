package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.UserTrustScoreData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserTrustScores extends Collection<UserTrustScoreData> {

    public void init() {
        super.init();
    }
}