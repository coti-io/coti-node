package io.coti.basenode.http;

public class BaseNodeHttpStringConstants {
    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_ERROR = "Error";

    public static final String ADDRESS_INVALID_ERROR_MESSAGE = "Address %s  is invalid";
    public static final String ADDRESS_TRANSACTIONS_SERVER_ERROR = "Address transactions server error";

    public static final String TRANSACTION_ALREADY_EXIST_MESSAGE = "Transaction already exists!";
    public static final String TRANSACTION_CREATED_MESSAGE = "Transaction created";
    public static final String TRANSACTION_CREATION_FAILED_MESSAGE = "Transaction creation failed";
    public static final String TRANSACTION_DETAILS_SERVER_ERROR = "Transaction details server error";
    public static final String TRANSACTION_DOESNT_EXIST_MESSAGE = "Transaction doesn't exist";
    public static final String TRANSACTION_RESPONSE_ERROR = "Transaction response error";
    public static final String TRANSACTION_ROLLBACK_MESSAGE = "Transaction creation failed";
    public static final String TRANSACTION_SOURCE_NOT_FOUND = "There is no valid source. Please try again later";

    public static final String TRANSACTION_CURRENTLY_MISSING_MESSAGE = "Transaction is currently missing. Waiting for propagation from neighbors!";
    public static final String WAITING_FOR_TRANSACTION_PARENT_MESSAGE = "Waiting_for_transaction_parent!";

    public static final String AUTHENTICATION_FAILED_MESSAGE = "Authentication failed!";
    public static final String INSUFFICIENT_FUNDS_MESSAGE = "Balance for address is insufficient!";
    public static final String ILLEGAL_TRANSACTION_MESSAGE = "Illegal transaction - sum of base transactions must be 0!";
    public static final String INVALID_TRUST_SCORE_MESSAGE = "Invalid sender trust score!";

    public static final String API_CLIENT_ERROR = "Api Client Error";
    public static final String API_SERVER_ERROR = "Api Server Error";

    public static final String INVALID_PARAMETERS_MESSAGE = "Invalid request parameters.";
    public static final String GENERAL_EXCEPTION_ERROR = "Error In Service";
}
