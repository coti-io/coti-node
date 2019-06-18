package io.coti.basenode.http;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Data
public class AddressFileRequest extends Request {
    @NotNull
    private MultipartFile file;
}
