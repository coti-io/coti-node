package io.coti.basenode.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetTokenSymbolDetailsRequest extends Request {

    @NotEmpty
    private String symbol;

}
