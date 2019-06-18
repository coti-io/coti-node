package io.coti.historynode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class GetTransactionsRequest extends Request {
    @NotEmpty(message = "Transactions hashes must not be empty")
    private List<Hash> addressesHashes;
    private Date startingDate;
    private Date endingDate;

    public GetTransactionsRequest() {
        addressesHashes = new ArrayList<>();
    }
}
