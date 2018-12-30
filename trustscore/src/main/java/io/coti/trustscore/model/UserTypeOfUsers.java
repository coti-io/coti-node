package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.Events.UserTypeOfUserData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class UserTypeOfUsers extends Collection<UserTypeOfUserData> {

    @PostConstruct
    public void init() {
        super.init();

    }
}
