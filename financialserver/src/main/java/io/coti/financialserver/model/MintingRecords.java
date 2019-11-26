package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.MintingRecordData;
import org.springframework.stereotype.Service;

@Service
public class MintingRecords extends Collection<MintingRecordData> {

    public void init() {
        super.init();
    }

}
