package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISubscriberMessageType;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.BaseNodeServiceManager;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public enum SubscriberMessageType implements ISubscriberMessageType {
    TRANSACTION_DATA(TransactionData.class) {
        @Override
        public Consumer<IPropagatable> getHandler(NodeType publisherNodeType) {
            return transactionData -> transactionService.handlePropagatedTransaction((TransactionData) transactionData);
        }
    },
    REJECTED_TRANSACTION_DATA(RejectedTransactionData.class) {
        @Override
        public Consumer<IPropagatable> getHandler(NodeType publisherNodeType) {
            return rejectedTransactionData -> transactionService.handlePropagatedRejectedTransaction((RejectedTransactionData) rejectedTransactionData);
        }
    },
    ADDRESS_DATA(AddressData.class) {
        @Override
        public Consumer<IPropagatable> getHandler(NodeType publisherNodeType) {
            return addressData -> addressService.handlePropagatedAddress((AddressData) addressData);
        }
    },
    DSP_CONSENSUS_RESULT(DspConsensusResult.class) {
        @Override
        public Consumer<IPropagatable> getHandler(NodeType publisherNodeType) {
            return dspConsensusResult -> dspVoteService.handleVoteConclusion((DspConsensusResult) dspConsensusResult);
        }
    },
    NETWORK_DATA(NetworkData.class) {
        @Override
        public Consumer<IPropagatable> getHandler(NodeType publisherNodeType) {
            return networkData -> networkService.handleNetworkChanges((NetworkData) networkData);
        }
    },
    TRANSACTIONS_STATE_DATA(TransactionsStateData.class) {
        @Override
        public Consumer<IPropagatable> getHandler(NodeType publisherNodeType) {
            return transactionsStateData -> transactionHelper.handleReportedTransactionsState((TransactionsStateData) transactionsStateData);
        }
    };

    protected ITransactionService transactionService = BaseNodeServiceManager.transactionService;
    protected IAddressService addressService = BaseNodeServiceManager.addressService;
    protected IDspVoteService dspVoteService = BaseNodeServiceManager.dspVoteService;
    protected INetworkService networkService = BaseNodeServiceManager.networkService;
    protected ITransactionHelper transactionHelper = BaseNodeServiceManager.nodeTransactionHelper;
    private final Class<? extends IPropagatable> messageTypeClass;

    SubscriberMessageType(Class<? extends IPropagatable> messageTypeClass) {
        this.messageTypeClass = messageTypeClass;
    }

    public Class<? extends IPropagatable> getMessageTypeClass() {
        return messageTypeClass;
    }

}
