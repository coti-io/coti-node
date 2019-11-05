package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class UploadCMDTokenIconData implements ISignValidatable {

    @NotNull
    private Hash userHash;
    @NotEmpty
    private String fileName;
    @NotNull
    private MultipartFile file;
    @NotNull
    private @Valid Hash currencyHash;
    @NotNull
    private Instant creationDate;
    @NotNull
    private @Valid SignatureData userSignature;

    public UploadCMDTokenIconData() {
        this.creationDate = Instant.now();
    }

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

}
