package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.UserDisputeEventData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class UserDisputeEvents extends Collection<UserDisputeEventData> {

    public UserDisputeEvents() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
