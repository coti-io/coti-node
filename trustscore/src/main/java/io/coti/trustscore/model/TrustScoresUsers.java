package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.TrustScoreUserData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TrustScoresUsers extends Collection<TrustScoreUserData> {

    public TrustScoresUsers() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
