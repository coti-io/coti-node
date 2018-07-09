package io.coti.common.model;

import io.coti.common.data.ConfirmationData;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class UnconfirmedTransactions extends Collection<ConfirmationData> {

    public UnconfirmedTransactions() {
    }

    public void init() {
        super.init();
    }
}