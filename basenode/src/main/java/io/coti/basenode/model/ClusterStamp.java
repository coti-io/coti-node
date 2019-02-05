package io.coti.basenode.model;

import io.coti.basenode.data.ClusterStampData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ClusterStamp extends Collection<ClusterStampData> {

    public ClusterStamp() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
