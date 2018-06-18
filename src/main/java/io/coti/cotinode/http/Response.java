package io.coti.cotinode.http;

import io.coti.cotinode.http.interfaces.IResponse;
import lombok.Data;

@Data
public abstract class Response implements IResponse {
    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_ERROR = "Error";
    public static final String MESSAGE_SUCCESS = "Operation Successful";
    public static final String ERROR_MESSAGE_UNAUTHORIZED = "Unauthorized";
    public static final String ERROR_MESSAGE_INCORRECT_ARGUMENTS = "Incorrect Arguments Received";


    public String message;
    public String status;
    public String errorType;

    public Response(String status, String message) {
        this.status = status;
        this.message = message;
    }

}
