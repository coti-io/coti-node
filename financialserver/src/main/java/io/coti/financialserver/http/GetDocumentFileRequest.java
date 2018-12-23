package io.coti.financialserver.http;

import io.coti.financialserver.http.data.GetDocumentFileData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetDocumentFileRequest {
    @NotNull
    private @Valid GetDocumentFileData documentFileData;
}
