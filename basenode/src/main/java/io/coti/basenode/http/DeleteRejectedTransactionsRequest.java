package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class DeleteRejectedTransactionsRequest {

    @NotEmpty
    private List<@Valid Hash> transactionHashes;
}
