package io.coti.basenode.model;

import io.coti.basenode.data.InitialFundData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ClusterStamps extends Collection<InitialFundData> {    //TODO: It should be ClusterStampData

    public ClusterStamps() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
