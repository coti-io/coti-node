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
    Hash userHash;
    @NotNull
    Hash disputeHash;
    @NotNull
    List<Long> itemIds;
    @NotNull
    String r;
    @NotNull
    String s;
    @NotNull
    MultipartFile file;

    public DisputeDocumentData getDisputeDocumentData() {
        return new DisputeDocumentData(userHash, disputeHash, itemIds, new SignatureData(r, s));
    }

}
