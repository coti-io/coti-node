package io.coti.cotinode.service;

import io.coti.cotinode.controllers.SubscriptionController;
import io.coti.cotinode.data.Hash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class BalanceSubscriptionService {
    private SubscriptionController subscriptionController;
    private List<Hash> subscriptions = new LinkedList<>();

    public void updateClients(Hash addressHash, double balance) {
        if(subscriptions.contains(addressHash)) {
            subscriptionController.sendBalanceUpdate(addressHash, balance);
        }
    }

    public void subscribe(Hash addressHash, SubscriptionController subscriptionController) {
        this.subscriptionController = subscriptionController;
        subscriptions.add(addressHash);
    }
}
