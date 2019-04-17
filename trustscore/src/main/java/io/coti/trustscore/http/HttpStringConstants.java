package io.coti.trustscore.http;

import io.coti.basenode.http.BaseNodeHttpStringConstants;

public class HttpStringConstants extends BaseNodeHttpStringConstants {

    public static final String NON_EXISTING_USER_MESSAGE = "User does not exist!";
    public static final String USER_TYPE_ALREADY_UPDATED = "User type is already updated";
    public static final String KYC_TRUST_SCORE_AUTHENTICATION_ERROR = "Kyc Trust Score Authentication Failed";
    public static final String KYC_TRUST_SCORE_ERROR = "Kyc Trust Score Creation or Update Failed";
    public static final String KYC_TRUST_DIFFERENT_TYPE = "Kyc Trust Score Update Failed, please use '/usertype' API call to change the type of user";
    public static final String TRUST_SCORE_EXIST = "Kyc Trust Score already exists, please use '/insertevent' API call";
    public static final String TRUST_SCORE_NOT_EXIST = "Trust score does not exist for user hash %s";
    public static final String ADD_KYC_SERVER_EVENT_ERROR = "Adding kyc server event failed";
    public static final String KYC_SERVER_EVENT_EXIST = "Kyc server event already exist";

    public static final String ILLEGAL_EVENT_FROM_KYC_SERVER = "ILLEGAL_EVENT_FROM_KYC_SERVER";
    public static final String TRUST_SCORE_EVENT_AUTHENTICATION_ERROR = "Event Trust Score Authentication Failed";
    public static final String SET_USER_TYPE_AUTHENTICATION_ERROR = "Set user type Authentication Failed";
    public static final String BAD_SIGNATURE_ON_TRUST_SCORE_FOR_TRANSACTION = "Event Trust Score Authentication Failed";
    public static final String USER_TYPE_SET_ERROR = "User type Updated Failed";
    public static final String USER_HASH_IS_NOT_IN_DB = "User hash not in DB";
    public static final String USER_NOT_MERCHANT = "User %s is not a merchant";

    public static final String FULL_NODE_FEE_VALIDATION_ERROR = "Full node fee validation failed";
    public static final String NETWORK_FEE_VALIDATION_ERROR = "Network fee validation failed";
    public static final String MERCHANT_ADRRESS_GET_ERROR = "Error at getting merchant address: %s";

}
