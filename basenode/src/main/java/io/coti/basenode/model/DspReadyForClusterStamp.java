package io.coti.basenode.model;

import io.coti.basenode.data.DspReadyForClusterStampData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DspReadyForClusterStamp extends Collection<DspReadyForClusterStampData> {

    public DspReadyForClusterStamp() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
