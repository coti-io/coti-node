package io.coti.basenode.http;

import io.coti.basenode.data.NodeRegistrationData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import javax.validation.Valid;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNodeRegistrationResponse extends BaseResponse {

    @Nullable
    @Valid
    private NodeRegistrationData nodeRegistrationData;
}
