package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RecourseClaimData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RecourseClaims extends Collection<RecourseClaimData> {

    public RecourseClaims() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
