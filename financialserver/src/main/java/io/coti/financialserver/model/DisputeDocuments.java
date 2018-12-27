package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeDocumentData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DisputeDocuments extends Collection<DisputeDocumentData> {

    public DisputeDocuments() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
