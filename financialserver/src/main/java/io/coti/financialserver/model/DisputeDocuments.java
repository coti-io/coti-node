package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeDocumentData;

@Service
public class DisputeDocuments extends Collection<DisputeDocumentData> {

    public DisputeDocuments() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
