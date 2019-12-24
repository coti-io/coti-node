package io.coti.trustscore.http;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SetUserTypeRequest extends SignedRequest {
    @NotNull
    public String userType;

}
