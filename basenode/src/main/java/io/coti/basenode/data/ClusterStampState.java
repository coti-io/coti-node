package io.coti.basenode.data;

import lombok.extern.slf4j.Slf4j;

/**
 * Defines the Cluster Stamp states and legal transitions.
 */
@Slf4j
public enum ClusterStampState {
    OFF("PREPARING"){

        @Override
        public ClusterStampState nextState(ClusterStampPreparationData clusterStampPreparationData) {
            logStateTransition();
            return PREPARING;
        }

    },
    PREPARING("READY"){

        @Override
        public ClusterStampState nextState(DspReadyForClusterStampData dspReadyForClusterStampData) {
            logStateTransition();
            return READY;
        }

    },
    READY("IN_PROCESS"){

        @Override
        public ClusterStampState nextState(ZeroSpendReadyForClusterStampData zeroSpendReadyForClusterStampData) {
            logStateTransition();
            return IN_PROCESS;
        }

        @Override
        public ClusterStampState nextState(ClusterStampData clusterStampConsensusResult) {
            logStateTransition();
            return IN_PROCESS;
        }
    },
    IN_PROCESS("OFF"){

        @Override
        public  ClusterStampState nextState(ClusterStampConsensusResult clusterStampConsensusResult){
            logStateTransition();
            return OFF;
        }

    };

    private static final String STATE_TRANSITION_ERROR = "Error, Wrong cluster stamp state transition! Can't move from state {} to state {} using {} request";

    private static final String STATE_TRANSITION_LOG = "Cluster stamp state Transition from {} to {}";

    private String nextState;

    /**
     * Constructor
     * @param nextState the next state.
     */
    ClusterStampState(final String nextState){
        this.nextState = nextState;
    }

    /**
     * Returns the next state.
     * @return next state.
     */
    public String getNextState() {
        return nextState;
    }

    /**
     * Returns the next state.
     * @param clusterStampPreparationData the request according to which next state should be decided.
     * @return the next state.
     */
    public ClusterStampState nextState(ClusterStampPreparationData clusterStampPreparationData){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "ClusterStampPreparationData");
        return this;
    }

    /**
     * Returns the next state.
     * @param dspReadyForClusterStampData the request according to which next state should be decided.
     * @return the next state.
     */
    public ClusterStampState nextState(DspReadyForClusterStampData dspReadyForClusterStampData){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "DspReadyForClusterStampData");
        return this;
    }

    /**
     * Returns the next state.
     * @param zeroSpendReadyForClusterStampData the request according to which next state should be decided.
     * @return the next state.
     */
    public ClusterStampState nextState(ZeroSpendReadyForClusterStampData zeroSpendReadyForClusterStampData){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "ZeroSpendReadyForClusterStampData");
        return this;
    }

    /**
     * Returns the next state.
     * @param clusterStampConsensusResult the request according to which next state should be decided.
     * @return the next state.
     */
    public ClusterStampState nextState(ClusterStampConsensusResult clusterStampConsensusResult){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "ClusterStampConsensusResult");
        return this;
    }

    /**
     * Returns the next state.
     * @param clusterStamp the request according to which next state should be decided.
     * @return the next state.
     */
    public ClusterStampState nextState(ClusterStampData clusterStamp){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "ClusterStampData");
        return this;
    }

    /**
     * Generates log message for transition of state.
     */
    public void logStateTransition(){
        log.info(STATE_TRANSITION_LOG, this.name(), this.getNextState());
    }

}
