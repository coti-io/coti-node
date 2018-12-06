package io.coti.trustscore.http;

import io.coti.basenode.http.BaseNodeHttpStringConstants;

public class HttpStringConstants extends BaseNodeHttpStringConstants {

    public static final String NON_EXISTING_USER_MESSAGE = "User does not exist!";
    public static final String KYC_TRUST_SCORE_AUTHENTICATION_ERROR = "Kyc Trust Score Authentication Failed";
    public static final String KYC_TRUST_SET_ERROR = "Kyc Trust Score Creation or Update Failed";
    public static final String TRUST_SCORE_EVENT_AUTHENTICATION_ERROR = "Event Trust Score Authentication Failed";
    public static final String BAD_SIGNATURE_ON_TRUST_SCORE_FOR_TRANSACTION = "Event Trust Score Authentication Failed";
    public static final String FULL_NODE_FEE_VALIDATION_ERROR = "Full Node Fee Validation Failed";
    public static final String ROLLING_RESERVE_VALIDATION_ERROR = "Rolling Reserve Validation Failed";
}
