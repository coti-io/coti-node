package io.coti.cotinode.http;

public class HttpStringConstants {
    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_ERROR = "Error";

    public static final String ADDRESS_CREATED_MESSAGE = "Address %s created";
    public static final String ADDRESS_ALREADY_EXISTS_MESSAGE = "Address %s already exists";
    public static final String ADDRESS_CREATION_ERROR_MESSAGE = "Address %s had a creation error";
    public static final String ADDRESS_LENGTH_ERROR_MESSAGE = "Address %s length is not valid";


    public static final String TRANSACTION_CREATED_MESSAGE = "Transaction created";

    public static final String TRANSACTION_FROM_PROPAGATION_MESSAGE = "";
    public static final String WAITING_FOR_TRANSACTION_PARENT_MESSAGE = "Waiting_for_transaction_parent!";
    public static final String TRANSACTION_ALREADY_EXIST_MESSAGE = "Transaction already exist!";
    public static final String AUTHENTICATION_FAILED_MESSAGE = "Authentication failed!";
    public static final String INSUFFICIENT_FUNDS_MESSAGE = "Balance for address is insufficient!";
    public static final String ILLEGAL_TRANSACTION_MESSAGE = "Illegal transaction - sum of base transactions must be 0!";

    public static final String API_CLIENT_ERROR = "Api Client Error";
    public static final String API_SERVER_ERROR = "Api Server Error";

    public static final String INVALID_PARAMETERS_MESSAGE = "Invalid request parameters.";
    public static final String INNER_EXCEPTION_MESSAGE = "Invalid request parameters.";
}
