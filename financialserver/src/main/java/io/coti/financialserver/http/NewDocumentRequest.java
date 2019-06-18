package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.Request;
import io.coti.financialserver.data.DisputeDocumentData;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class NewDocumentRequest extends Request {
    @NotNull
    private Hash userHash;
    @NotNull
    private Hash disputeHash;
    @NotNull
    private List<Long> itemIds;
    @NotNull
    private String r;
    @NotNull
    private String s;
    @NotNull
    private MultipartFile file;

    public DisputeDocumentData getDisputeDocumentData() {
        return new DisputeDocumentData(userHash, disputeHash, itemIds, new SignatureData(r, s));
    }

}
