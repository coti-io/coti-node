package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.UserDisputesData;

@Service
public class UserDisputes extends Collection<UserDisputesData> {

    public UserDisputes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
