package io.coti.basenode.model;

import io.coti.basenode.data.DspNodeReadyForClusterStampData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DspNodeReadyForClusterStamp extends Collection<DspNodeReadyForClusterStampData> {

    public DspNodeReadyForClusterStamp() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
