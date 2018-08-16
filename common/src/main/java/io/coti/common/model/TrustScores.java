package io.coti.common.model;

import io.coti.common.data.TrustScoreData;
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