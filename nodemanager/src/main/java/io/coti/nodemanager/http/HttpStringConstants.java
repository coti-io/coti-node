package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseNodeHttpStringConstants;

public class HttpStringConstants extends BaseNodeHttpStringConstants {

    public static final String NODE_ADDED_TO_NETWORK = "Node %s is added to Coti Network";
    public static final String STAKING_NODE_ADDED = "Staking node %s is added";
    public static final String NODE_STAKE_SET_AUTHENTICATION_ERROR = "Setting Node Stake Authentication Failed";
    public static final String ADDING_EVENT_PAIR_FAILED = "Can Not Add FullNode Beginning Event Pair For The First Event Is Not Active Or Not Exists";
    public static final String ADDING_EVENT_PAIR_INCORRECT_TIME = "Can Not Add FullNode Beginning Event Because The Time Given Is After Existing Event";
    public static final String NODE_HISTORY_RECORD_HAS_BEEN_ADDED_MANUALLY = "A History Record for Node %s Has Been Added Manually";
    public static final String ADDING_EVENT_INCORRECT_FUTURE_TIME = "Can not add future event";
    public static final String ADDING_EVENT_NONACTIVATED_NODE_HASH = "Can not add event to a non activated node";
    public static final String ADDING_EVENT_INCORRECT_INACTIVE_STATUS = "Can not add an Inactive event not part of a pair";
    public static final String ADDING_EVENT_CONSECUTIVE_SAME_STATUS = "Can not add a consecutive identical status";
}
