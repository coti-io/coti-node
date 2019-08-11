package io.coti.historynode.http;

import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;

@Data
public class GetTransactionsByDateRequest extends Request {

    @NotEmpty
    @Valid
    private LocalDate date;


}
