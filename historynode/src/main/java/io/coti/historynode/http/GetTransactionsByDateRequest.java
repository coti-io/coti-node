package io.coti.historynode.http;

import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.Instant;

@Data
public class GetTransactionsByDateRequest extends Request {

    private @NotEmpty Instant date;


}
