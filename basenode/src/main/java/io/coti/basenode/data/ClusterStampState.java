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
            logStateTransition(this.name(),this.getNextState());
            return PREPARING;
        }

        @Override
        public ClusterStampState nextState(DspReadyForClusterStampData dspReadyForClusterStampData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(ZeroSpendReadyForClusterStampData zeroSpendReadyForClusterStampData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public  ClusterStampState nextState(ClusterStampConsensusResult clusterStampConsensusResult){
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(ClusterStampData clusterStampConsensusResult) {
            throw new UnsupportedOperationException();
        }
    },
    PREPARING("READY"){

        @Override
        public ClusterStampState nextState(ClusterStampPreparationData clusterStampPreparationData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(DspReadyForClusterStampData dspReadyForClusterStampData) {
            logStateTransition(this.name(),this.getNextState());
            return READY;
        }

        @Override
        public ClusterStampState nextState(ZeroSpendReadyForClusterStampData zeroSpendReadyForClusterStampData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public  ClusterStampState nextState(ClusterStampConsensusResult clusterStampConsensusResult){
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(ClusterStampData clusterStampConsensusResult) {
            throw new UnsupportedOperationException();
        }
    },
    READY("IN_PROCESS"){

        @Override
        public ClusterStampState nextState(ClusterStampPreparationData clusterStampPreparationData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(DspReadyForClusterStampData dspReadyForClusterStampData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(ZeroSpendReadyForClusterStampData zeroSpendReadyForClusterStampData) {
            logStateTransition(this.name(),this.getNextState());
            return IN_PROCESS;
        }

        @Override
        public  ClusterStampState nextState(ClusterStampConsensusResult clusterStampConsensusResult){
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(ClusterStampData clusterStampConsensusResult) {
            logStateTransition(this.name(),this.getNextState());
            return IN_PROCESS;
        }
    },
    IN_PROCESS("OFF"){

        @Override
        public ClusterStampState nextState(ClusterStampPreparationData clusterStampPreparationData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(DspReadyForClusterStampData dspReadyForClusterStampData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ClusterStampState nextState(ZeroSpendReadyForClusterStampData zeroSpendReadyForClusterStampData) {
            throw new UnsupportedOperationException();
        }

        @Override
        public  ClusterStampState nextState(ClusterStampConsensusResult clusterStampConsensusResult){
            logStateTransition(this.name(),this.getNextState());
            return OFF;
        }

        @Override
        public ClusterStampState nextState(ClusterStampData clusterStampConsensusResult) {
            throw new UnsupportedOperationException();
        }
    };

    private String nextState;

    /**
     * Constructor
     * @param nextState the next state.
     */
    ClusterStampState(final String nextState){
        this.nextState = nextState;
    }

    /**
     * Returnds the next state.
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
    public abstract ClusterStampState nextState(ClusterStampPreparationData clusterStampPreparationData);

    /**
     * Returns the next state.
     * @param dspReadyForClusterStampData the request according to which next state should be decided.
     * @return the next state.
     */
    public abstract ClusterStampState nextState(DspReadyForClusterStampData dspReadyForClusterStampData);

    /**
     * Returns the next state.
     * @param zeroSpendReadyForClusterStampData the request according to which next state should be decided.
     * @return the next state.
     */
    public abstract ClusterStampState nextState(ZeroSpendReadyForClusterStampData zeroSpendReadyForClusterStampData);

    /**
     * Returns the next state.
     * @param clusterStampConsensusResult the request according to which next state should be decided.
     * @return the next state.
     */
    public abstract ClusterStampState nextState(ClusterStampConsensusResult clusterStampConsensusResult);

    /**
     * Returns the next state.
     * @param clusterStamp the request according to which next state should be decided.
     * @return the next state.
     */
    public abstract ClusterStampState nextState(ClusterStampData clusterStamp);

    /**
     * Generates log for transition from current state to next state.
     * @param currentState the current state.
     * @param nextState the next state.
     */
    public void logStateTransition(String currentState, String nextState){
        log.info("Cluster stamp state Transition from {} to {}", currentState, nextState);
    }
}
