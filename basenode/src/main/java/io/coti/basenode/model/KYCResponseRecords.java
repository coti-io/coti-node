package io.coti.basenode.model;

import io.coti.basenode.data.KYCResponseRecordData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
@Service
public class KYCResponseRecords extends Collection<KYCResponseRecordData> {

    public KYCResponseRecords(){}

    @PostConstruct
    public void init() {
        super.init();
    }

}
