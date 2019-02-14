package io.coti.basenode.model;

import io.coti.basenode.data.ClusterStampData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ClusterStamps extends Collection<ClusterStampData> {

    public ClusterStamps() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
