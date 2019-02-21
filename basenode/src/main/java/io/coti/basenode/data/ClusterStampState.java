package io.coti.basenode.data;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ClusterStampState {
    OFF{
        @Override
        public ClusterStampState nextState(){
            logStateTransition(this.name(),"PREPARING");
            return PREPARING;
        }
    },
    PREPARING{
        @Override
        public ClusterStampState nextState(){
            logStateTransition(this.name(),"READY");
            return READY;
        }
    },
    READY{
        @Override
        public ClusterStampState nextState(){
            logStateTransition(this.name(),"IN_PROCESS");
            return IN_PROCESS;
        }
    },
    IN_PROCESS{
        @Override
        public ClusterStampState nextState(){
            logStateTransition(this.name(),"OFF");
            return OFF;
        }
    };

    public abstract ClusterStampState nextState();

    public void logStateTransition(String CurrentState, String nextState){
        log.info("Cluster stamp state Transition from {} to {}", CurrentState, nextState);
    }
}
