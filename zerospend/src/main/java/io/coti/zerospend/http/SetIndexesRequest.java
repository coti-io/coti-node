package io.coti.zerospend.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import java.util.HashSet;

@Data
public class SetIndexesRequest {

    private HashSet<@Valid Hash> transactionHashes;
}
