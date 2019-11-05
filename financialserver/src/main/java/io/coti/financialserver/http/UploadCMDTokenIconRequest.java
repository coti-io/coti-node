package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.http.data.UploadCMDTokenIconData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class UploadCMDTokenIconRequest extends Request {

    @NotNull
    private @Valid UploadCMDTokenIconData uploadCMDTokenIconData;

    public UploadCMDTokenIconRequest(@NotNull @Valid UploadCMDTokenIconData uploadCMDTokenIconData) {
        this.uploadCMDTokenIconData = uploadCMDTokenIconData;
    }
}
