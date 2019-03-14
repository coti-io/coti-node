package io.coti.basenode.data;

import lombok.extern.slf4j.Slf4j;

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

    ClusterStampState(final String nextState){
        this.nextState = nextState;
    }

    public String getNextState() {
        return nextState;
    }

    public ClusterStampState nextState(ClusterStampPreparationData clusterStampPreparationData){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "ClusterStampPreparationData");
        return this;
    }

    public ClusterStampState nextState(DspReadyForClusterStampData dspReadyForClusterStampData){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "DspReadyForClusterStampData");
        return this;
    }

    public ClusterStampState nextState(ZeroSpendReadyForClusterStampData zeroSpendReadyForClusterStampData){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "ZeroSpendReadyForClusterStampData");
        return this;
    }

    public ClusterStampState nextState(ClusterStampConsensusResult clusterStampConsensusResult){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "ClusterStampConsensusResult");
        return this;
    }

    public ClusterStampState nextState(ClusterStampData clusterStamp){
        log.error(STATE_TRANSITION_ERROR ,this.name(),this.getNextState(), "ClusterStampData");
        return this;
    }

    public void logStateTransition(){
        log.info(STATE_TRANSITION_LOG, this.name(), this.getNextState());
    }

}
