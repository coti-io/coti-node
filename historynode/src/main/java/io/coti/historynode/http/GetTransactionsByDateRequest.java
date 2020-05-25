package io.coti.historynode.http;

import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;

@Data
public class GetTransactionsByDateRequest implements IRequest {

    @NotEmpty
    @Valid
    private LocalDate date;


}
