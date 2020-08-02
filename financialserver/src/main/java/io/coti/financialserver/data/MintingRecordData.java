package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashMap;

@Data
public class MintingRecordData implements IEntity {

    private static final long serialVersionUID = 5889649093085123791L;
    @NotNull
    private Hash hash;
    private HashMap<Instant, MintingHistoryData> mintingHistory;

    public MintingRecordData(Hash hash) {
        this.hash = hash;
        this.mintingHistory = new HashMap<>();
    }
}
