package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class CMDTokenIconData implements IEntity {
    @NotNull
    private @Valid Hash hash;
    @NotNull
    private @Valid Hash userHash;
    @NotNull
    private Boolean iconUpdated;
    @NotNull
    private Instant lastUpdatedTime;
    @NotEmpty
    private String imageFileExtension;

    public CMDTokenIconData(Hash hash, Hash userHash, String imageFileExtension) {
        this.hash = hash;
        this.userHash = userHash;
        this.iconUpdated = true;
        this.lastUpdatedTime = Instant.now();
        this.imageFileExtension = imageFileExtension;
    }
}
