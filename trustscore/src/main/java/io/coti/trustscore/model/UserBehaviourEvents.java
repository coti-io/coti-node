package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.UserEvents;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class UserBehaviourEvents extends Collection<UserEvents> {

    public UserBehaviourEvents() {
    }

    @PostConstruct
    public void init() {
        super.init();

    }
}
