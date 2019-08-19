package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.interfaces.ICurrencyService;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService implements ICurrencyService {

    public static final Hash NATIVE_CURRENCY_HASH = null;
}
