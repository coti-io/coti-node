package io.coti.basenode.http;

public class BaseNodeHttpStringConstants {
    public static final String STATUS_SUCCESS = "Success";
    public static final String STATUS_ERROR = "Error";
    public static final String SERVER_ERROR = "Server error";
    public static final String CREATED_MESSAGE = "Created";

    public static final String INVALID_ADDRESS = "Address %s  is invalid";
    public static final String ADDRESS_TRANSACTIONS_SERVER_ERROR = "Address transactions server error";

    public static final String TRANSACTION_ALREADY_EXIST_MESSAGE = "Transaction already exists!";
    public static final String TRANSACTION_CREATED_MESSAGE = "Transaction created";
    public static final String TRANSACTION_DETAILS_SERVER_ERROR = "Transaction details server error";
    public static final String TRANSACTION_DOESNT_EXIST_MESSAGE = "Transaction doesn't exist";
    public static final String TRANSACTION_RESPONSE_ERROR = "Transaction response error";
    public static final String TRANSACTION_ROLLBACK_MESSAGE = "Transaction creation failed";
    public static final String TRANSACTION_SOURCE_NOT_FOUND = "There is no valid source. Please try again later";
    public static final String TRANSACTION_RESENT_MESSAGE = "Transaction resent to the network";
    public static final String TRANSACTION_RESENT_INVALID_SIGNATURE_MESSAGE = "Invalid transaction resend request signature";
    public static final String TRANSACTION_RESENT_PROCESSING_MESSAGE = "Transaction requested to resend is still processed";
    public static final String TRANSACTION_RESENT_NOT_AVAILABLE_MESSAGE = "Transaction requested to resend is not available in the database";
    public static final String TRANSACTION_RESENT_NOT_ALLOWED_MESSAGE = "Transaction is requested to resend not by the transaction sender";

    public static final String AUTHENTICATION_FAILED_MESSAGE = "Authentication failed!";
    public static final String INSUFFICIENT_FUNDS_MESSAGE = "Balance for address is insufficient!";
    public static final String ILLEGAL_BASE_TRANSACTIONS_AMOUNT = "Illegal transaction - sum of base transactions must be 0!";
    public static final String INVALID_TRUST_SCORE_MESSAGE = "Invalid sender trust score!";

    public static final String API_CLIENT_ERROR = "Api Client Error";
    public static final String API_SERVER_ERROR = "Api Server Error";

    public static final String INVALID_PARAMETERS_MESSAGE = "Invalid request parameters.";
    public static final String INVALID_SIGNER = "Invalid signer hash";
    public static final String GENERAL_EXCEPTION_ERROR = "Error In Service";

    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String INVALID_FULL_NODE_FEE = "Invalid full node fee";
    public static final String INVALID_SIGNATURE = "Invalid signature";
    public static final String INVALID_NODE_REGISTRAR = "Invalid node registrar";
    public static final String INVALID_NODE_IP_VERSION = "Invalid node IP version";
    public static final String INVALID_NODE_SERVER_URL = "Invalid node server URL";
    public static final String INVALID_NODE_SERVER_URL_UNKNOWN_HOST = "Invalid node server URL %s: unknown host. Dns record not found";
    public static final String INVALID_NODE_SERVER_URL_EMPTY_HOST = "Invalid node server URL %s: empty host";
    public static final String INVALID_NODE_SERVER_URL_HOST_RESERVED = "Invalid node server URL %s: host %s reserved to another user";
    public static final String INVALID_NODE_SERVER_URL_SSL_REQUIRED = "Invalid node server URL %s: SSL required";
    public static final String INVALID_NODE_IP_FOR_SERVER_URL = "Invalid node server URL %s: host %s is not matching with ip %s. The expected ip is %s";
    public static final String INVALID_NODE_REGISTRATION_SIGNATURE = "Invalid node registration signature";
    public static final String INVALID_NETWORK_TYPE = "Network type should be %s. Invalid network type %s";
    public static final String INVALID_TRANSACTION_TIME_FIELD = "Invalid transaction time field. Current node time: %s";
    public static final String INVALID_AMOUNT = "Invalid amount";
    public static final String INVALID_AMOUNT_VS_FULL_NODE_FEE = "Transaction amount should be greater than minimum full node fee %s";

    public static final String UNAUTHORIZED_IP_ACCESS = "Unauthorized ip access";
    public static final String METHOD_NOT_SUPPORTED = "Unsupported method";

    public static final String ADDRESS_BATCH_UPLOADED = "Address batch uploaded";
    public static final String ADDRESS_BATCH_UPLOAD_ERROR = "Address batch upload error: %s";

    public static final String DOCUMENT_NOT_FOUND = "Document not found";

    public static final String NOT_BACKUP_NODE = "The node is not a backup node";
}
