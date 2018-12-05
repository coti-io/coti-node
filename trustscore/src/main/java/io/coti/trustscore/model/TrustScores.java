package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.TrustScoreData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class TrustScores extends Collection<TrustScoreData> {

    @PostConstruct
    public void init() {
        super.init();
    }
}