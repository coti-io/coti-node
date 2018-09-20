package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.UserCurrentTsData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class UsersCurrentTs extends Collection<UserCurrentTsData> {

    public UsersCurrentTs() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
