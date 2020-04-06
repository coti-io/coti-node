package io.coti.financialserver.http;

import io.coti.basenode.http.BaseNodeHttpStringConstants;

public class HttpStringConstants extends BaseNodeHttpStringConstants {

    public static final String SUCCESS = "Success";

    public static final String DISPUTE_ALREADY_EXISTS_FOR_TRANSACTION = "Dispute already exists for this transaction";
    public static final String DISPUTE_COMMENT_CREATE_UNAUTHORIZED = "Unauthorized dispute comment creation request";
    public static final String DISPUTE_COMMENT_UNAUTHORIZED = "Unauthorized dispute comment request";
    public static final String DISPUTE_DOCUMENT_CREATE_UNAUTHORIZED = "Unauthorized dispute document creation request";
    public static final String DISPUTE_DOCUMENT_UNAUTHORIZED = "Unauthorized dispute document request";
    public static final String DISPUTE_MERCHANT_NOT_FOUND = "Merchant not found";
    public static final String DISPUTE_NOT_FOUND = "Dispute not found";
    public static final String DISPUTE_UNAUTHORIZED = "Unauthorized dispute request";
    public static final String DISPUTE_INVALID_SIDE = "Invalid dispute side";
    public static final String DISPUTE_ITEM_NOT_FOUND = "Dispute item not found";
    public static final String DISPUTE_ITEM_STATUS_FINAL = "Dispute item status is final. It can not be changed";
    public static final String DISPUTE_ITEM_STATUS_INVALID_CHANGE = "Dispute item status change is invalid";
    public static final String DISPUTE_ITEM_STATUS_INVALID_ACTIONSIDE = "%s is not valid side for item status change to %s";
    public static final String DISPUTE_ITEM_UPDATE_SUCCESS = "Dispute item successfully updated";
    public static final String DISPUTE_ITEM_VOTE_SUCCESS = "Dispute item vote successfully submitted";
    public static final String DISPUTE_ITEMS_EXIST_ALREADY = "At least one of the dispute items is already was(or right now) in dispute";
    public static final String DISPUTE_ITEMS_INVALID = "Invalid dispute items";
    public static final String DISPUTE_STATUS_FINAL = "Dispute status is final. It can not be changed";
    public static final String DISPUTE_STATUS_INVALID_CHANGE = "Dispute status change is invalid";
    public static final String DISPUTE_TRANSACTION_NOT_FOUND = "Transaction hash not found";
    public static final String DISPUTE_TRANSACTION_NOT_PAYMENT = "Transaction is not of type payment";
    public static final String DISPUTE_TRANSACTION_SENDER_INVALID = "Invalid transaction sender";
    public static final String OPEN_DISPUTE_IN_PROCESS_FOR_THIS_TRANSACTION = "Open dispute already in process for this transaction";

    public static final String DISTRIBUTION_REQUEST_HANDLED_PREVIOUSLY = "Distribution request handled previously";

    public static final String DOCUMENT_EXISTS_ERROR = "Document already exists";

    public static final String DOCUMENT_UPLOAD_ERROR = "Some error occurred during upload";
    public static final String COMMENT_NOT_FOUND = "Comment not found";
    public static final String ITEM_NOT_FOUND = "Item not found";
    public static final String DISPUTE_ITEM_PASSED_RECALL_STATUS = "Dispute item passed recall status";
    public static final String DISPUTE_NOT_IN_CLAIM_STATUS = "Dispute not in claim status";
    public static final String ALREADY_GOT_YOUR_VOTE = "You already voted on this item";

    public static final String DUPLICATE_FUND_NAME = "Duplicated fund name";
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    public static final String HEADER_ATTACHMENT_PREFIX = "attachment; filename=";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String S3_SUFFIX_METADATA_KEY = "x-amz-meta-suffix";


    public static final String NOT_COTI_POOL = "Transaction receiver isn't Coti pool";
    public static final String ALREADY_GOT_THIS_RECOURSE_CLAIM = "Recourse claim transaction already processed";
    public static final String NOT_ENOUGH_MONEY_IN_TRANSACTION = "Not enough money in transaction";

    public static final String INTERNAL_ERROR = "Internal error";
    public static final String PARSED_WITH_ERROR = "Parsing file encountered error.";
    public static final String DISTRIBUTION_FILE_ALREADY_PROCESSED = "Distribution file already processed today";
    public static final String DISTRIBUTION_DATE_ERROR = "No distribution on specified date";
    public static final String DISTRIBUTION_DATE_EMPTY_ENTRIES_ERROR = "Empty distribution entries on specified date";
    public static final String DISTRIBUTION_HASH_DOESNT_EXIST = "Distribution hash doesn't exist";
    public static final String DISTRIBUTION_INITIATED_OR_CANCELLED = "Distribution is either initiated or cancelled";
    public static final String INVALID_UPDATED_DISTRIBUTION_AMOUNT = "Invalid updated distribution amount";

    public static final String CANT_SAVE_FILE_ON_DISK = "Can't save file on disk.";
    public static final String BAD_CSV_FILE_LINE_FORMAT = "Bad csv file line format";
    public static final String ACCEPTED = "Accepted";
    public static final String LOCK_UP_DATE_IS_INVALID = "Lock-up date is invalid";
    public static final String RECEIVER_ADDRESS_INVALID = "Receiver address is invalid";
    public static final String DATE_UNIQUENESS_WAS_NOT_MAINTAINED = "Date uniqueness was not maintained";
    public static final String DISTRIBUTION_POOL_BALANCE_CHECKS_FAILED = "Distribution pool balance checks failed";
    public static final String TRANSACTION_CREATED_SUCCESSFULLY = "Transaction created successfully";
    public static final String TRANSACTION_CREATION_FAILED = "Transaction creation failed";

    public static final String DISTRIBUTION_FILE_RECORD_DELETED = "Distribution file record deleted";

    public static final String TOKEN_GENERATION_REQUEST_INVALID_SIGNATURE = "Signature check failed for token generation request";
    public static final String TOKEN_GENERATION_REQUEST_NOT_REGULAR_CMD_TOKEN = "Attempt to generate not Regular type token";
    public static final String TOKEN_GENERATION_REQUEST_FAILURE = "Failed to create requested token";
    public static final String TOKEN_MINTING_FAILURE = "Failed to create requested token fee";
    public static final String TOKEN_MINTING_REQUEST_INVALID_SIGNATURE = "Signature check failed for token minting request";
    public static final String TOKEN_MINTING_REQUEST_INVALID_CURRENCY = "Non-existing currency in token minting fee request";
    public static final String TOKEN_MINTING_REQUEST_INVALID_ORIGINATOR = "Request for token minting fee by user not the token originator";
    public static final String TOKEN_MINTING_REQUEST_INVALID_FOR_THE_QUOTE = "The minting request details for minting differ from provided quote";
    public static final String TOKEN_MINTING_REQUEST_INVALID_ADDRESS = "The receiver address provided for minting is invalid";
    public static final String TOKEN_MINTING_REQUEST_INVALID_AMOUNT = "The amount to be minted exceeds available funds";

    public static final String TOKEN_MINTING_FEE_QUOTE_REQUEST_INVALID_CURRENCY = "Non-existing currency in token minting fee quote request";
    public static final String TOKEN_MINTING_FEE_QUOTE_REQUEST_INVALID_ORIGINATOR = "Request for token minting fee quote by user not the token originator";

}
