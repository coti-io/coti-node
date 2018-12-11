package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RecourseClaimData;

@Service
public class RecourseClaims extends Collection<RecourseClaimData> {

    public RecourseClaims() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
