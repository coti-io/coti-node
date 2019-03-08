package io.coti.basenode.communication;

import io.coti.basenode.communication.interfaces.ISubscriberMessageType;
import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EnumSet;
import java.util.function.Consumer;

public enum SubscriberMessageType implements ISubscriberMessageType {
    TransactionData {
        @Override
        public Consumer<Object> getHandler(NodeType publisherNodeType) {
            return transactionData -> transactionService.handlePropagatedTransaction((TransactionData) transactionData);
        }
    },
    AdressData {
        @Override
        public Consumer<Object> getHandler(NodeType publisherNodeType) {
            return addressData -> addressService.handlePropagatedAddress((AddressData) addressData);
        }
    },
    DspConsensusResult {
        @Override
        public Consumer<Object> getHandler(NodeType publisherNodeType) {
            return dspConsensusResult -> dspVoteService.handleVoteConclusion((DspConsensusResult) dspConsensusResult);
        }
    },
    NetworkData {
        @Override
        public Consumer<Object> getHandler(NodeType publisherNodeType) {
            return networkData -> networkService.handleNetworkChanges((NetworkData) networkData);
        }
    };

    protected ITransactionService transactionService;
    protected IAddressService addressService;
    protected IDspVoteService dspVoteService;
    protected INetworkService networkService;

    @Component
    public static class BaseTransactionCryptoInjector {
        @Autowired
        private ITransactionService transactionService;
        @Autowired
        private IAddressService addressService;
        @Autowired
        private IDspVoteService dspVoteService;
        @Autowired
        private INetworkService networkService;

        @PostConstruct
        public void postConstruct() {
            for (SubscriberMessageType subscriberMessageType : EnumSet.allOf(SubscriberMessageType.class)) {
                subscriberMessageType.transactionService = transactionService;
                subscriberMessageType.addressService = addressService;
                subscriberMessageType.dspVoteService = dspVoteService;
                subscriberMessageType.networkService = networkService;
            }
        }
    }

}
