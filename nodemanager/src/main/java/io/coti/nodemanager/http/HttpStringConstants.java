package io.coti.nodemanager.http;

import io.coti.basenode.http.BaseNodeHttpStringConstants;

public class HttpStringConstants extends BaseNodeHttpStringConstants {

    public static final String NODE_ADDED_TO_NETWORK = "Node %s is added to Coti Network";
    public static final String STAKING_NODE_ADDED = "Staking node %s is added";
    public static final String NODE_STAKE_SET_AUTHENTICATION_ERROR = "Setting Node Stake Authentication Failed";

    public static final String ADDING_SINGLE_EVENT_INCORRECT_NODE_TYPE = "Expected Node type of FullNode for node %s";
    public static final String ADDING_SINGLE_EVENT_INCORRECT_NETWORK_NODE_STATUS = "Expected network node status Inactive for node %s";
    public static final String ADDING_SINGLE_EVENT_NON_ACTIVATED_NODE = "Expected network node %s to be already activated";
    public static final String ADDING_SINGLE_EVENT_INCORRECT_LAST_STATUS_NOT_ACTIVE = "Expected current network node status to be Active for node %s";
    public static final String ADDING_SINGLE_EVENT_INCORRECT_TIME_BEFORE_LAST_EXISTING_STATUS = "Expected current network node time to be after last existing status for node %s";
    public static final String ADDING_SINGLE_EVENT_INCORRECT_FUTURE_TIME = "Expected network node event time can not be in the future for node %s";
    public static final String ADDING_SINGLE_EVENT_ADDED_MANUALLY = "Network node event was added manually for node %s";

    public static final String ADDING_PAIR_EVENTS_INCORRECT_NODE_TYPE = "Expected Node type of FullNode for node %s";
    public static final String ADDING_PAIR_EVENTS_CONSECUTIVE_STATUS = "Introducing consecutive identical status is not allowed for node %s";
    public static final String ADDING_PAIR_EVENTS_INCORRECT_FUTURE_TIME = "Expected network node event time can not be in the future for node %s";
    public static final String ADDING_PAIR_EVENTS_START_IN_FUTURE = "Expected network node pair events time can not be in the future for node %s";
    public static final String ADDING_PAIR_EVENTS_AFTER_LAST_EVENT_OPEN_ENDED = "Expected network node pair events time can not be open ended in the future for node %s";
    public static final String ADDING_PAIR_EVENTS_WITH_EXISTING_EVENTS_INSIDE = "Expected network node pair events time can not have existing event in pair time range for node %s";
    public static final String ADDING_PAIR_EVENTS_ADDED_MANUALLY = "Network node pair events were added manually for node %s";

    public static final String NODE_EVENTS_SERVER_ERROR = "Node events server error: %s";
    public static final String NODE_INVALID_HASH = "Invalid node hash %s";


}
