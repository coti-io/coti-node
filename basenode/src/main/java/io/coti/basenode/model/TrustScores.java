package io.coti.basenode.model;

import io.coti.basenode.data.TrustScoreData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class TrustScores extends Collection<TrustScoreData> {

    public TrustScores() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}