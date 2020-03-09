package io.coti.basenode.model;

import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import org.springframework.stereotype.Service;

@Service
public class UnconfirmedReceivedTransactionHashes<T extends UnconfirmedReceivedTransactionHashData> extends Collection<T> {

}
