package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISubscriberMessageType;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public enum SubscriberMessageType implements ISubscriberMessageType {
    TRANSACTION_DATA(TransactionData.class) {
        @Override
        public Consumer<Object> getHandler(NodeType publisherNodeType) {
            return transactionData -> transactionService.handlePropagatedTransaction((TransactionData) transactionData);
        }
    },
    ADDRESS_DATA(AddressData.class) {
        @Override
        public Consumer<Object> getHandler(NodeType publisherNodeType) {
            return addressData -> addressService.handlePropagatedAddress((AddressData) addressData);
        }
    },
    DSP_CONSENSUS_RESULT(DspConsensusResult.class) {
        @Override
        public Consumer<Object> getHandler(NodeType publisherNodeType) {
            return dspConsensusResult -> dspVoteService.handleVoteConclusion((DspConsensusResult) dspConsensusResult);
        }
    },
    NETWORK_DATA(NetworkData.class) {
        @Override
        public Consumer<Object> getHandler(NodeType publisherNodeType) {
            return networkData -> networkService.handleNetworkChanges((NetworkData) networkData);
        }
    };

    protected ITransactionService transactionService;
    protected IAddressService addressService;
    protected IDspVoteService dspVoteService;
    protected INetworkService networkService;
    private Class<? extends IPropagatable> messageTypeClass;

    SubscriberMessageType(Class<? extends IPropagatable> messageTypeClass) {
        this.messageTypeClass = messageTypeClass;
    }

    public Class<? extends IPropagatable> getMessageTypeClass() {
        return messageTypeClass;
    }

}
