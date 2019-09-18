package io.coti.financialserver.model;

import io.coti.basenode.data.UserTokenGenerationData;
import io.coti.basenode.model.Collection;
import org.springframework.stereotype.Service;

@Service
public class UserTokenGenerations extends Collection<UserTokenGenerationData> {

    public void init() {
        super.init();
    }

}
