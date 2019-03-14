package io.coti.fullnode.model;

import io.coti.basenode.model.Collection;
import io.coti.fullnode.data.ExplorerIndexData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ExplorerIndexes extends Collection<ExplorerIndexData> {

    public ExplorerIndexes() {
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
    }
}
