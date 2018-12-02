package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class NewDocumentRequest extends Request {
    @NotNull(message = "File is required.")
    private MultipartFile file;
}
